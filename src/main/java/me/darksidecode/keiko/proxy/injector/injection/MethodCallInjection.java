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

package me.darksidecode.keiko.proxy.injector.injection;

import lombok.NonNull;
import me.darksidecode.keiko.proxy.injector.Inject;
import me.darksidecode.keiko.util.References;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

/**
 * Inserts a call to a given public static method in the beginning or in the end of the given method.
 */
public class MethodCallInjection extends Injection {

    /**
     * The method call to which is injected.
     */
    private final String targetClass, targetMethod;

    /**
     * Relative method code injection position.
     */
    private final Inject.Position position;

    public MethodCallInjection(@NonNull String inClass,
                               @NonNull String inMethodName,
                               @NonNull String inMethodDesc,
                               @NonNull String targetClass,
                               @NonNull String targetMethod,
                               @NonNull Inject.Position position) {
        super(inClass, inMethodName, inMethodDesc);

        if (position == Inject.Position.UNUSED)
            throw new IllegalArgumentException("@Inject.Position cannot be UNUSED for MethodCallInjection");

        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
        this.position = position;
    }

    @Override
    protected void apply(@NonNull ClassNode cls, @NonNull MethodNode mtd) {
        LabelNode pos = new LabelNode();

        if (position == Inject.Position.END || mtd.instructions.size() == 0)
            mtd.instructions.add(pos);
        else
            mtd.instructions.insertBefore(mtd.instructions.getFirst(), pos);

        inject(pos, mtd);
    }

    private void inject(LabelNode pos, MethodNode mtd) {
        // TODO -- test if array parameters (e.g. String[]) also work, fix if they don't
        // Load all parameters onto the stack so that they are passed to the target method.
        // Wrap them all in a MethodParam so that we can also use params whose type is not
        // available on the class path on build (such as NMS classes -- we only have those at
        // runtime, and we don't want to depend on a hardcoded version of the NMS package).
        // On the other hand, wrapping with MethodParam instead of simply using Object in
        // params makes it easier to use the types that we do have at on the class path on
        // build (such as String, Integer, and so on). We have to box primitives (for example,
        // int -> java.lang.Integer) because of how Java generics work (type erasure).
        StringBuilder targetMethodDesc = new StringBuilder("(");
        Type[] paramTypes = Type.getArgumentTypes(mtd.desc);

        if (paramTypes.length > 0) {
            mtd.maxStack += paramTypes.length;
            int off = References.isStatic(mtd) ? 0 : 1; // offset 1 for var "this" if the method is non-static

            for (int var = 0; var < paramTypes.length; var++) {
                targetMethodDesc.append("Lme/darksidecode/keiko/proxy/injector/MethodParam;");

                Type paramType = paramTypes[var];
                int loadOp = paramType.getOpcode(ILOAD); // transforms ILOAD into ?LOAD based on paramType ("?")
                mtd.instructions.insertBefore(pos, new VarInsnNode(loadOp, off + var));
                boxIfNecessary(pos, paramType, mtd);

                mtd.instructions.insertBefore(pos, new MethodInsnNode(
                        INVOKESTATIC,
                        "me/darksidecode/keiko/proxy/injector/MethodParam",
                        "wrap",
                        "(Ljava/lang/Object;)Lme/darksidecode/keiko/proxy/injector/MethodParam;"
                ));
            }
        }

        // Finally, insert the call to our target public static method.
        targetMethodDesc.append(")V");
        mtd.instructions.insertBefore(pos, new MethodInsnNode(
                INVOKESTATIC, targetClass, targetMethod, targetMethodDesc.toString()));
    }

    private static void boxIfNecessary(LabelNode pos, Type paramType, MethodNode mtd) {
        // Looks like a reinvented wheel. Isn't there alredy a method for that?..
        switch (paramType.getDescriptor()) {
            case "B":
                box(pos, Byte.class, "B", mtd);
                break;

            case "C":
                box(pos, Character.class, "C", mtd);
                break;

            case "D":
                box(pos, Double.class, "D", mtd);
                break;

            case "F":
                box(pos, Float.class, "F", mtd);
                break;

            case "I":
                box(pos, Integer.class, "I", mtd);
                break;

            case "J":
                box(pos, Long.class, "J", mtd);
                break;

            case "S":
                box(pos, Short.class, "S", mtd);
                break;

            case "Z":
                box(pos, Boolean.class, "Z", mtd);
                break;
        }
    }

    private static void box(LabelNode pos, Class<?> type, String baseTypeDesc, MethodNode mtd) {
        String boxedType = type.getName().replace('.', '/');
        mtd.instructions.insertBefore(pos, new MethodInsnNode(
                INVOKESTATIC, boxedType, "valueOf",
                "(" + baseTypeDesc + ")L" + boxedType + ";"));
    }

}
