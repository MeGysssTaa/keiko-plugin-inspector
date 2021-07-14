/*
 * Copyright (C) 2019-2021 German Vekhorev (DarksideCode)
 *
 * This file is part of Keiko Plugin Inspector.
 *
 * Keiko Plugin Inspector is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Keiko Plugin Inspector is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Keiko Plugin Inspector.  If not, see <https://www.gnu.org/licenses/>.
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

public class KeikoSecurityManager extends DomainAccessController implements ExtendedDAC {

    private final Map<Operation, KeikoLogger.Level> logLevels;

    private final Map<Operation, List<Rule>> rules;

    private final YamlHandle conf;

    public KeikoSecurityManager() {
        conf = RuntimeProtectConfig.getHandle();
        logLevels = new HashMap<>();
        rules = new HashMap<>();

        for (Operation op : Operation.values()) {
            rules.put(op, new ArrayList<>());
            loadLogLevel(op);
            loadRules(op);
        }
    }

    private void loadLogLevel(Operation op) {
        String levelStr = conf
                .getString("domain_access_control." + op.name().toLowerCase() + ".log", "off")
                .replace(' ', '_').toUpperCase();

        KeikoLogger.Level level = KeikoLogger.Level.OFF;

        try {
            level = KeikoLogger.Level.valueOf(levelStr);
        } catch (IllegalArgumentException ex) {
            // Invalid log level. Fall back to "disable logging".
            Keiko.INSTANCE.getLogger().error(
                    "Invalid log level \"%s\" for DAC operation %s. Falling back to \"off\".",
                    op.name(), levelStr);
        }

        logLevels.put(op, level);
    }

    private void loadRules(Operation op) {
        for (String ruleStr : getRules(op)) {
            try {
                rules.get(op).add(new Rule(ruleStr));
            } catch (Exception ex) {
                // Invalid rule. Skip it and warn.
                String cause = (ex.getCause() == null) ? "-" : ex.getCause().getMessage();
                Keiko.INSTANCE.getLogger().warningLocalized(
                        "runtimeProtect.dac.ignoringInvalidRule",
                        op.localizedName, ruleStr, ex.getMessage(), cause);
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
                            op.localizedName, "... " + arg,
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
                                op.localizedName, "... " + arg,
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
                        op.localizedName, "... " + arg,
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
                Operation.COMMAND_EXECUTION, I18n.get("runtimeProtect.dac.cmd", cmd));
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
            // at line with 'RuntimeUtils.resolveCallerPlugin()' on some JVMs. This is because 'resolveCallerPlugin()'
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
        if (Rule.isLoaded()) {
            // Essentially, every plugin will end up calling KeikoSecurityManager methods (checkRead, etc.),
            // though not explicitly (but internally by Java APIs). So we exclude this package automatically.
            // Also exclude injected calls so that Keiko Megane event injections and other injections work.
            // The "injection" package is excluded exlicitly as well because the first injected call causes
            // the *Injection class to be loaded, which causes another checkPackageAccess where the utility
            // method RuntimeUtils#isInjectedKeikoCall still returns false. Another way to circumvent this
            // issue could be to load all the *Injection classes eagerly, but I think that's not so necessary.
            boolean exclude = 
                    pkg.startsWith("me.darksidecode.keiko.runtimeprotect.dac")
                 || pkg.startsWith("me.darksidecode.keiko.proxy.injector.injection")
                 || RuntimeUtils.isInjectedKeikoCall();

            checkAccess(arg -> !exclude && StringUtils.matchWildcards(
                    pkg, arg), Operation.PACKAGE_ACCESS, I18n.get("runtimeProtect.dac.pkg", pkg));
        }
    }

    @Override
    public void checkClassAccess(String fqcn) {
        // Note that, unlike in checkPackageAccess, this check only triggers on direct class lookups.
        // That is, plugins will not be "accidentially" causing checkClassAccess due to injected calls
        // or DAC (SecurityManager) checks. For this reason, we don't hardcode any exclusions. Only
        // direct class usages from plugins will trigger this check.
        checkAccess(arg -> StringUtils.matchWildcards(
                fqcn, arg), Operation.CLASS_ACCESS, I18n.get("runtimeProtect.dac.class", fqcn));
    }

    @Override
    public void checkOpAdd(String player) {
        checkAccess(arg -> StringUtils.matchWildcards(
                player, arg), Operation.MINECRAFT_OP_ADD,
                I18n.get("runtimeProtect.dac.player", player));
    }

    @Override
    public void checkOpRemove(String player) {
        checkAccess(arg -> StringUtils.matchWildcards(
                player, arg), Operation.MINECRAFT_OP_REMOVE,
                I18n.get("runtimeProtect.dac.player", player));
    }

    @Override
    public void checkCommandDispatch(String command) {
        checkAccess(arg -> StringUtils.matchWildcards(
                command, arg), Operation.MINECRAFT_COMMAND_DISPATCH,
                I18n.get("runtimeProtect.dac.cmd", command));
    }

    private void checkNoArgs(Operation op) {
        // No required arg(s) for this operation (always "*")
        checkAccess(arg -> true, op, "-");
    }

    private void checkAccess(Function<String, Boolean> ruleFunc, Operation op, String details) {
        Identity caller = RuntimeUtils.resolveCallerPlugin();

        // If caller is null, then this means that the caller is either
        // the Minecraft server/Bukkit/Bungee, or some other dark magic.
        if (caller != null) {
            logAccess(caller, op, details);

            // Self-defense.
            if (RuntimeProtectConfig.getSelfDefense()
                    && (op == Operation.FILE_READ || op == Operation.FILE_WRITE || op == Operation.FILE_DELETE)
                    && (details.contains(Keiko.INSTANCE.getEnv().getWorkDir().getAbsolutePath())) // "File: {AbsPath}"
                    ||  details.contains(Keiko.INSTANCE.getEnv().getKeikoExecutable().getAbsolutePath())) { // ^
                Keiko.INSTANCE.getLogger().warningLocalized(
                        "runtimeProtect.dac.vioDetected", caller, op.localizedName, details);
                throw new SecurityException("access denied by Keiko Domain Access Control (self-defense)");
            }

            // User-defined rules/filters.
            Rule.Type finalRule = Rule.Type.ALLOW;

            for (Rule rule : rules.get(op)) {
                String arg = rule.getArg();

                if (rule.getIdentityFilter() == Rule.IdentityFilter.ALL)
                    arg = arg.replace("{plugin_name}",     caller.getPluginName())
                             .replace("{plugin_jar_path}", caller.getFilePath  ());

                boolean callerMatch = rule.filterCaller(caller);
                boolean argumentMatch = ruleFunc.apply(arg);
                boolean fullMatch = callerMatch && argumentMatch;

                if (fullMatch) // both caller and call argument satisfy this rule's filter
                    finalRule = rule.getFilterType(); // overwrite final rule (bottom-most rules have higher priority)
            }

            if (finalRule == Rule.Type.DENY) {
                Keiko.INSTANCE.getLogger().warningLocalized(
                        "runtimeProtect.dac.vioDetected", caller, op.localizedName, details);
                throw new SecurityException("access denied by Keiko Domain Access Control");
            }
        }
    }

    private void logAccess(Identity caller, Operation op, String details) {
        Keiko.INSTANCE.getLogger().logLocalized(logLevels.get(op),
                "runtimeProtect.dac.actionDebug", caller, op.localizedName, details);
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
        CLASS_ACCESS,
        MINECRAFT_OP_ADD,
        MINECRAFT_OP_REMOVE,
        MINECRAFT_COMMAND_DISPATCH,
        MISCELLANEOUS;

        private String localizedName;

        static {
            Operation[] ops = values();

            // Use localized names for better accessibility.
            for (Operation op : ops) 
                op.localizedName = I18n.get("runtimeProtect.dac.op." + op.name().toLowerCase());
        }
    }

}
