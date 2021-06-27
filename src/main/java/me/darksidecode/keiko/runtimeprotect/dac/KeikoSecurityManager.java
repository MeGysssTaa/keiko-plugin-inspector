/*
 * Copyright 2021 German Vekhorev (DarksideCode)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.darksidecode.keiko.runtimeprotect.dac;

import lombok.RequiredArgsConstructor;
import me.darksidecode.keiko.config.RuntimeProtectConfig;
import me.darksidecode.keiko.config.YamlHandle;
import me.darksidecode.keiko.i18n.I18n;
import me.darksidecode.keiko.io.KeikoLogger;
import me.darksidecode.keiko.proxy.Keiko;
import me.darksidecode.keiko.registry.Identity;
import me.darksidecode.keiko.util.RuntimeUtils;
import me.darksidecode.keiko.util.StringUtils;

import java.io.File;
import java.net.InetAddress;
import java.security.Permission;
import java.util.*;
import java.util.function.Function;

public class KeikoSecurityManager extends DomainAccessController {

    private final Map<Operation, Rule.Type> defaultRules;
    private final Map<Operation, List<Rule>> rules;

    private final YamlHandle conf;

    public KeikoSecurityManager() {
        conf = RuntimeProtectConfig.getHandle();
        defaultRules = new HashMap<>();
        rules = new HashMap<>();

        for (Operation op : Operation.values()) {
            String defaultRuleStr = conf.get(
                    "domain_access_control." + op.name().toLowerCase() + ".default",
                    Rule.Type.ALLOW.name());

            try {
                defaultRules.put(op, Rule.Type.valueOf(defaultRuleStr.toUpperCase().trim()));
            } catch (NullPointerException | IllegalArgumentException ex) {
                defaultRules.put(op, Rule.Type.ALLOW);
                Keiko.INSTANCE.getLogger().warningLocalized(
                        "runtimeProtect.dac.invalidDefCfg", op.fancyName(), defaultRuleStr);
            }

            rules.put(op, new ArrayList<>());
            loadRules(rules, op);
        }
    }

    private void loadRules(Map<Operation, List<Rule>> rules, Operation op) {
        for (String ruleStr : getRules(op)) {
            try {
                Rule rule = new Rule(ruleStr);

                if (rule.getFilterType() == defaultRules.get(op))
                    Keiko.INSTANCE.getLogger().warningLocalized(
                            "runtimeProtect.dac.ignoringContraRule", op.fancyName(), ruleStr);
                else
                    rules.get(op).add(rule);
            } catch (Exception ex) {
                // Invalid rule. Skip it and warn.
                String cause = (ex.getCause() == null) ? "?" : ex.getCause().getMessage();
                Keiko.INSTANCE.getLogger().warningLocalized(
                        "runtimeProtect.dac.ignoringInvalidRule",
                        op.fancyName(), ruleStr, ex.getMessage(), cause);
            }
        }
    }

    private List<String> getRules(Operation op) {
        return conf.get("domain_access_control." + op.name().toLowerCase() + ".rules");
    }

    @Override
    public void checkConnect(String host, int port, Object context) {
        this.checkConnect(host, port); // ignore context
    }

    @Override
    public void checkConnect(String host, int port) {
        checkConnectionAccess(host, port, Operation.CONNECTION_OPEN);
    }

    @Override
    public void checkListen(int port) {
        checkConnectionAccess("localhost", port, Operation.CONNECTION_LISTEN);
    }

    @Override
    public void checkAccept(String host, int port) {
        checkConnectionAccess(host, port, Operation.CONNECTION_ACCEPT);
    }

    @Override
    public void checkMulticast(InetAddress maddr) {
        checkConnectionAccess(maddr.getHostAddress(), -0xCAFE, Operation.CONNECTION_MULTICAST);
    }

    private void checkConnectionAccess(String host, int port, Operation op) {
        checkAccess(arg -> {
            if (arg.contains(" PORT ")) {
                String[] args = arg.split(" PORT ");

                if (args.length != 2) {
                    Keiko.INSTANCE.getLogger().warningLocalized(
                            "runtimeProtect.dac.ignoringInvalidRule",
                            op.fancyName(), "... " + arg,
                            "unexpected number of arguments separated by \" PORT \"",
                            "expected 2, got " + args.length);

                    return false;
                }

                String allowedHost = args[0].trim();
                String allowedPortStr = StringUtils.replacePortByName(args[1].trim());
                int allowedPort;

                if (allowedPortStr.equals("*"))
                    allowedPort = -0xCAFE; // allow all ports
                else {
                    try {
                        allowedPort = Integer.parseInt(allowedPortStr);

                        if (allowedPort < 0 || allowedPort > 65535)
                            throw new NumberFormatException();
                    } catch (NumberFormatException ex) {
                        Keiko.INSTANCE.getLogger().warningLocalized(
                                "runtimeProtect.dac.ignoringInvalidRule",
                                op.fancyName(), "... " + arg,
                                "invalid port number",
                                "expected an integer in range 0 to 65535 or a special port name (example: \"HTTPS\")");

                        return false;
                    }
                }

                boolean allowHost = StringUtils.matchWildcards(host, allowedHost);
                boolean allowPort = allowedPort == -0xCAFE || port == allowedPort;

                return allowHost && allowPort;
            } else {
                Keiko.INSTANCE.getLogger().warningLocalized(
                        "runtimeProtect.dac.ignoringInvalidRule",
                        op.fancyName(), "... " + arg,
                        "missing port number",
                        "argument syntax: \"(host) PORT (port)");

                return false;
            }
        }, op, I18n.get("runtimeProtect.dac.hostPort", host, port));
    }

    @Override
    public void checkSetFactory() {
        checkNoArgs(Operation.SOCKET_FACTORY_SET);
    }

    @Override
    public void checkRead(String file, Object context) {
        this.checkRead(file); // ignore context
    }

    @Override
    public void checkRead(String file) {
        checkFileAccess(file, Operation.FILE_READ);
    }

    @Override
    public void checkWrite(String file) {
        checkFileAccess(file, Operation.FILE_WRITE);
    }

    @Override
    public void checkDelete(String file) {
        checkFileAccess(file, Operation.FILE_DELETE);
    }

    private void checkFileAccess(String file, Operation op) {
        file = new File(file).getAbsolutePath(); // transform 'file' to get the full path
        file = file.replace("\\", "/"); // Windows's directory separator + Regex != love
        String finalFile = file;

        checkAccess(arg -> StringUtils.matchWildcards(
                finalFile, arg), op, I18n.get("runtimeProtect.dac.file", file));
    }

    @Override
    public void checkLink(String lib) {
        String libPath = new File(lib).getAbsolutePath().
                replace("\\", "/") /* better Windows compatibility */;

        checkAccess(arg -> {
            boolean allowLibName = StringUtils.matchWildcards(lib, arg);
            boolean allowLibPath = StringUtils.matchWildcards(libPath, arg);

            return allowLibName || allowLibPath;
        }, Operation.NATIVES_LINKAGE, I18n.get("runtimeProtect.dac.nativeLib", lib, libPath));
    }

    @Override
    public void checkExec(String cmd) {
        checkAccess(arg -> StringUtils.matchWildcards(cmd, arg),
                Operation.COMMAND_EXECUTION, I18n.get("runtimeProtect.dac.sysCmd", cmd));
    }

    @Override
    public void checkExit(int status) {
        checkAccess(arg -> {
            int allowedStatus;

            if (arg.equals("*"))
                allowedStatus = -0xCAFE;
            else {
                try {
                    allowedStatus = Integer.parseInt(arg);
                } catch (NumberFormatException ex) {
                    Keiko.INSTANCE.getLogger().warningLocalized(
                            "runtimeProtect.dac.ignoringInvalidRule",
                            Operation.SYSTEM_EXIT, "... " + arg,
                            "invalid exit code",
                            "exit code must be an integer");

                    return false;
                }
            }

            return allowedStatus == -0xCAFE || status == allowedStatus;
        }, Operation.SYSTEM_EXIT, I18n.get("runtimeProtect.dac.statusCode"));
    }

    @Override
    public void checkPropertiesAccess() {
        checkNoArgs(Operation.PROPERTIES_ACCESS);
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
        this.checkPermission(perm); // ignore context
    }

    @Override
    public void checkPermission(Permission perm) {
        String action = perm.getName();

        if (perm instanceof PropertyPermission) {
            PropertyPermission propertyPerm = (PropertyPermission) perm;

            String key = propertyPerm.getName();
            String actions = propertyPerm.getActions();

            if (actions.contains("write"))
                checkPropertyAccess(key, Operation.PROPERTY_WRITE);

            if (actions.contains("read"))
                checkPropertyAccess(key, Operation.PROPERTY_READ);
        } else if (!(action.equals("suppressAccessChecks"))) {
            // A plugin attempts to execute potentially malicious code that would otherwise bypass Keiko.
            // https://docs.oracle.com/javase/8/docs/technotes/guides/security/permissions.html
            //
            // We ignore permission 'suppressAccessChecks' fully because this permission is
            // required to access members of Java classes that are otherwise not accessible
            // using Reflection (see java.lang.reflect.AccessibleObject#setAccessible).
            // If we did not ignore this permission, a StackOverflowError would be thrown
            // at line with 'RuntimeUtils.getCallerInfo()' on some JVMs. This is because 'getCallerInfo()'
            // calls `PluginContext#getClassOwner(String)', which uses Streams API, specifically,
            // 'findFirst()', which natively calls 'java.lang.invoke.InnerClassLambdaMetafactory#buildCallSite',
            // which requires the 'suppressAccessChecks' permission, thus executing method
            // 'SecurityManager#checkPermission', and therefore returning us to this point.
            checkAccess(arg -> StringUtils.matchWildcards(
                    action, arg), Operation.MISCELLANEOUS, I18n.get("runtimeProtect.dac.action", action));
        }
    }

    private void checkPropertyAccess(String property, Operation op) {
        checkAccess(arg -> StringUtils.matchWildcards(
                property, arg), op, I18n.get("runtimeProtect.dac.prop", property));
    }

    @Override
    public void checkPackageAccess(String pkg) {
        checkAccess(arg -> StringUtils.matchWildcards(
                pkg, arg), Operation.PACKAGE_ACCESS, I18n.get("runtimeProtect.dac.pkg", pkg));
    }

    private void checkNoArgs(Operation op) {
        // No required arg(s) for this operation (always "*")
        checkAccess(arg -> true, op, "-");
    }

    private void checkAccess(Function<String, Boolean> ruleFunc, Operation op, String details) {
        Identity caller = RuntimeUtils.getCaller();

        // If caller is null, then this means that the caller is either
        // the Minecraft server/Bukkit/Bungee, or some other dark magic.
        if (caller != null) {
            Rule.Type defaultRule = defaultRules.get(op);

            debugAccess(caller, op, details);
            boolean deny = defaultRule == Rule.Type.DENY;

            // Self-defense
            if (RuntimeProtectConfig.getSelfDefense()
                    && (op == Operation.FILE_WRITE) || (op == Operation.FILE_DELETE)
                    && details.contains(Keiko.INSTANCE.getWorkDir().getAbsolutePath())) { // "File: {file_name}"
                Keiko.INSTANCE.getLogger().warningLocalized(
                        "runtimeProtect.dac.vioDetected", caller, op.fancyName(), details);
                throw new SecurityException("access denied by Keiko Domain Access Control (self-defense)");
            }

            // Rules/filters
            for (Rule rule : rules.get(op)) {
                String arg = rule.getArg();

                if (rule.getIdentityFilter() == Rule.IdentityFilter.ALL)
                    arg = arg.
                            replace("{plugin_name}", caller.getPluginName()).
                            replace("{plugin_jar_path}", caller.getFilePath());

                boolean filtered = rule.filterCaller(caller);
                boolean allowArg = ruleFunc.apply(arg);
                boolean match = filtered && allowArg;

                if (match)
                    deny = rule.getFilterType() == Rule.Type.DENY;
            }

            if (deny) {
                Keiko.INSTANCE.getLogger().warningLocalized(
                        "runtimeProtect.vioDetected", caller, op.fancyName(), details);
                throw new SecurityException("access denied by Keiko Domain Access Control");
            }
        }
    }

    private void debugAccess(Identity caller, Operation op, String details) {
        boolean notify = conf.get("domain_access_control." + op.name().toLowerCase() + ".notify", false);
        KeikoLogger.Level level = notify ? KeikoLogger.Level.INFO : KeikoLogger.Level.DEBUG;
        Keiko.INSTANCE.getLogger().logLocalized(
                level, "runtimeProtect.dac.actionDebug", caller, op.fancyName(), details);
    }

    @RequiredArgsConstructor
    private enum Operation {
        CONNECTION_OPEN,
        CONNECTION_LISTEN,
        CONNECTION_ACCEPT,
        CONNECTION_MULTICAST,
        SOCKET_FACTORY_SET,
        FILE_READ,
        FILE_WRITE,
        FILE_DELETE,
        NATIVES_LINKAGE,
        COMMAND_EXECUTION,
        SYSTEM_EXIT,
        PROPERTIES_ACCESS,
        PROPERTY_WRITE,
        PROPERTY_READ,
        PACKAGE_ACCESS,
        MISCELLANEOUS;

        String fancyName() {
            return name().toLowerCase().replace('_', ' ');
        }
    }

}
