/*
 * Copyright 2020 DarksideCode
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
import me.darksidecode.keiko.KeikoPluginInspector;
import me.darksidecode.keiko.config.RuntimeProtectConfig;
import me.darksidecode.keiko.runtimeprotect.CallerInfo;
import me.darksidecode.keiko.util.Factory;
import me.darksidecode.keiko.util.RuntimeUtils;
import me.darksidecode.keiko.util.StringUtils;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.net.InetAddress;
import java.security.Permission;
import java.util.*;

public class KeikoSecurityManager extends DomainAccessController {

    private final Map<Operation, Rule.Type> defaultRules;
    private final Map<Operation, List<Rule>> rules;

    private final YamlConfiguration conf;

    public KeikoSecurityManager() {
        conf = RuntimeProtectConfig.getYaml();

        defaultRules = new HashMap<>();
        rules = new HashMap<>();

        for (Operation op : Operation.values()) {
            String defaultRuleStr = conf.getString(
                    "domain_access_control." + op.name().toLowerCase() + ".default");

            try {
                if (defaultRuleStr == null)
                    throw new NullPointerException();

                defaultRules.put(op, Rule.Type.valueOf(defaultRuleStr.toUpperCase().trim()));
            } catch (NullPointerException | IllegalArgumentException ex) {
                KeikoPluginInspector.warn("Invalid Domain Access Control default configuration " +
                        "for operation %s: %s, expected one of: %s. Falling back to 'ALLOW'",
                        op, defaultRuleStr, Arrays.toString(Rule.Type.values()));

                defaultRules.put(op, Rule.Type.ALLOW);
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
                    KeikoPluginInspector.warn("Ignoring contradictory rule %s for " +
                            "operation %s (default rule is of the same type).", rule.getFilterType(), op);
                else
                    rules.get(op).add(rule);
            } catch (Exception ex) {
                // Invalid rule. Skip it and warn.
                String cause = (ex.getCause() == null) ? "?" : ex.getCause().getMessage();
                KeikoPluginInspector.warn(
                        "Invalid Domain Access Control rules configuration for operation %s. " +
                                "These rules will be ignored. Details: %s (%s).", op, ex.getMessage(), cause);
            }
        }
    }

    private List<String> getRules(Operation op) {
        return conf.getStringList("domain_access_control." + op.name().toLowerCase() + ".rules");
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
                    KeikoPluginInspector.warn("Invalid rule for operation %s: " +
                                    "illegal number of arguments separated by ' PORT ': " +
                                    "expected exactly two ('host PORT port'): \"%s\". Ignoring this rule!",
                            op, arg);

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
                    } catch (NumberFormatException ex) {
                        KeikoPluginInspector.warn("Invalid rule for operation %s: " +
                                        "invalid port (port must be an integer in range 0 to 65535): %s." +
                                        "Ignoring this rule!",
                                op, allowedPortStr);

                        return false;
                    }

                    if ((allowedPort < 0) || (allowedPort > 65535)) {
                        KeikoPluginInspector.warn("Invalid rule for operation %s: " +
                                        "invalid port (port must be an integer in range 0 to 65535): %s." +
                                        "Ignoring this rule!",
                                op, allowedPortStr);

                        return false;
                    }
                }

                boolean allowHost = StringUtils.matchWildcards(host, allowedHost);
                boolean allowPort = allowedPort == -0xCAFE || port == allowedPort;

                return allowHost && allowPort;
            } else {
                KeikoPluginInspector.warn("Invalid rule for operation %s: missing port specification; " +
                        "required syntax: 'host PORT port'. Ignoring this rule!", op);

                return false;
            }
        }, op, "Host: " + host + " | Port: " + port);
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
                finalFile, arg), op, "File: " + file);
    }

    @Override
    public void checkLink(String lib) {
        String libPath = new File(lib).getAbsolutePath().
                replace("\\", "/") /* better Windows compatibility */;

        checkAccess(arg -> {
            boolean allowLibName = StringUtils.matchWildcards(lib, arg);
            boolean allowLibPath = StringUtils.matchWildcards(libPath, arg);

            return allowLibName || allowLibPath;
        }, Operation.NATIVES_LINKAGE, "Library: " + lib + " | Path: " + libPath);
    }

    @Override
    public void checkExec(String cmd) {
        checkAccess(arg -> StringUtils.matchWildcards(cmd, arg),
                Operation.COMMAND_EXECUTION, "Command: " + cmd);
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
                    KeikoPluginInspector.warn("Invalid rule for operation %s: " +
                                    "invalid exit status: %s (must be an integer). Ignoring this rule!",
                            Operation.SYSTEM_EXIT, arg);

                    return false;
                }
            }

            return allowedStatus == -0xCAFE || status == allowedStatus;
        }, Operation.SYSTEM_EXIT, "Status: " + status);
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
        if (perm instanceof PropertyPermission) {
            PropertyPermission propertyPerm = (PropertyPermission) perm;

            String key = propertyPerm.getName();
            String actions = propertyPerm.getActions();

            if (actions.contains("write"))
                checkPropertyAccess(key, Operation.PROPERTY_WRITE);

            if (actions.contains("read"))
                checkPropertyAccess(key, Operation.PROPERTY_READ);
        }
    }

    private void checkPropertyAccess(String property, Operation op) {
        checkAccess(arg -> StringUtils.matchWildcards(
                property, arg), op, "Property: " + property);
    }

    private void checkNoArgs(Operation op) {
        // No required arg(s) for this operation (always "*")
        checkAccess(arg -> true, op, "No Details");
    }

    private void checkAccess(Factory<Boolean, String> ruleFactory, Operation op, String details) {
        CallerInfo callerInfo = RuntimeUtils.getCallerInfo();

        // If callerInfo is null, then this means that the caller is either
        // Keiko, the Minecraft server/Bukkit/Spigot, or some other dark magic.
        if (callerInfo != null) {
            Rule.Type defaultRule = defaultRules.get(op);

            debugAccess(callerInfo, op, details);
            boolean deny = defaultRule == Rule.Type.DENY;

            // Self-defense
            if ((RuntimeProtectConfig.getSelfDefense())
                    && ((op == Operation.FILE_WRITE) || (op == Operation.FILE_DELETE))
                    && (details.contains("/" + KeikoPluginInspector.getWorkDir().getName()))) { // "File: {file_name}"
                KeikoPluginInspector.warn(
                        "(Self-Defense) Detected security violation by %s on operation %s (%s)",
                        callerInfo, op, details);

                throw new SecurityException("(Self-Defense) access denied by Keiko Domain Access Control");
            }

            // Rules/filters
            for (Rule rule : rules.get(op)) {
                String arg = rule.getArg();

                if (rule.getIdentityFilter() == Rule.IdentityFilter.ALL)
                    arg = arg.
                            replace("{plugin_name}", callerInfo.getPlugin().getName()).
                            replace("{plugin_jar_path}", callerInfo.getPlugin().getJar().getAbsolutePath().
                                    replace("\\", "/") /* better Windows compatibility */);

                boolean filtered = rule.filterCaller(callerInfo);
                boolean allowArg = ruleFactory.get(arg);
                boolean match = filtered && allowArg;

                if (match)
                    deny = rule.getFilterType() == Rule.Type.DENY;
            }

            if (deny) {
                KeikoPluginInspector.warn(
                        "Detected security violation by %s on operation %s (%s)",
                        callerInfo, op, details);

                throw new SecurityException("access denied by Keiko Domain Access Control");
            }
        }
    }

    private void debugAccess(CallerInfo callerInfo, Operation op, String details) {
        String message = String.format("Registered %s call initiated by %s. %s",
                op.name().toLowerCase().replace("_", " "), callerInfo, details);

        if (RuntimeProtectConfig.getYaml().getBoolean(
                "domain_access_control." + op.name().toLowerCase() + ".notify", false))
            KeikoPluginInspector.info(message);
        else
            KeikoPluginInspector.debug(message);
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
        PROPERTY_READ
    }

}
