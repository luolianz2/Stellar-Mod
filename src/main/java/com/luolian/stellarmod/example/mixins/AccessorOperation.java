package com.luolian.stellarmod.example.mixins;

public abstract class AccessorOperation {
    /**
     * <h1>访问器（Accessor）和调用器（Invoker）的使用</h1>
     * <h2>它们是什么？</h2>
     * 访问器用于暴露目标类中的私有字段，使得我们可以在其它类中调用它们。<br>
     * 调用器则用于暴露目标类中的私有方法，使得我们可以在其它类中调用它们。<br>
     * 假设存在一个这样的目标类：
     * <pre>{@code
     * public class TargetClass {
     *    private static int privateStaticField;
     *    private int privateField;
     *    private boolean privateMethod(int param) {
     *        // 私有实例方法的实现
     *    }
     *    private static boolean privateStaticMethod(int param) {
     *        // 私有静态方法的实现
     *     }
     * }
     * }</pre>
     * <h2>使用访问器和调用器暴露内容</h2>
     * 如果我们想要在其它类中访问其中的私有字段或方法，我们可以这样定义访问器来暴露它们：
     * <pre>{@code
     * @Mixin(TargetClass.class)
     * public interface TargetClassAccessor {
     *     @Accessor("privateField")
     *     int getPrivateField();
     * }
     * }</pre>
     * <pre>{@code
     * @Mixin(TargetClass.class)
     * public interface TargetClassInvoker {
     *     @Invoker("privateMethod")
     *     boolean callPrivateMethod(int param);
     * }
     * }</pre>
     * 使用时，我们可以通过强转来访问这些私有成员：
     * <pre>{@code
     * public class SomeOtherClass {
     *     public static void someMethod(TargetClass target) {
     *         int value = ((TargetClassAccessor) target).getPrivateField();
     *         boolean result = ((TargetClassInvoker) target).callPrivateMethod(myParam);
     *     }
     * }
     * }</pre>
     * <h2>使用访问器和调用器暴露静态内容</h2>
     * 顾名思义，就不解释了，直接看例子：
     * <pre>{@code
     * @Mixin(TargetClass.class)
     * public interface TargetClassAccessor {
     *     @Accessor("privateStaticField")
     *     static int getPrivateStaticField() {
     *         // 接口中的静态方法必须提供一个实现，虽然这个实现永远不会被调用，但它必须存在
     *         throw new AssertionError();
     *     }
     * }
     * }</pre>
     * <pre>{@code
     * @Mixin(TargetClass.class)
     * public interface TargetClassInvoker {
     *     @Invoker("privateStaticMethod")
     *     static boolean callPrivateStaticMethod(int param) {
     *         // 接口中的静态方法必须提供一个实现，虽然这个实现永远不会被调用，但它必须存在
     *         throw new AssertionError();
     *     }
     * }
     * }</pre>
     * 使用方法就简单了，直接调用静态方法：
     * <pre>{@code
     * public class SomeOtherClass {
     *     public static void someMethod() {
     *         int value = TargetClassAccessor.getPrivateStaticField();
     *         boolean result = TargetClassInvoker.callPrivateStaticMethod(myParam);
     *     }
     * }
     * }</pre>
     * <h2>使用访问器的修改内容</h2>
     * 访问器还可以用来赋值私有字段，还用上面的目标类为例：
     * <pre>{@code
     * @Mixin(TargetClass.class)
     * public interface TargetClassAccessor {
     *     @Accessor("privateField")
     *     void setPrivateField(int value);
     *
     *     @Accessor("privateStaticField")
     *     static void setPrivateStaticField(int value) {
     *         // 接口中的静态方法必须提供一个实现，虽然这个实现永远不会被调用，但它必须存在
     *         throw new AssertionError();
     *     }
     * }
     * }</pre>
     * 使用时同样通过强转来访问：
     * <pre>{@code
     * public class SomeOtherClass {
     *     public static void someMethod(TargetClass target) {
     *         ((TargetClassAccessor) target).setPrivateField(42);
     *         // 在此方法被调用后，target实例的privateField字段的值将被设置为42
     *         TargetClassAccessor.setPrivateStaticField(100);
     *         // 在此方法被调用后，TargetClass类的privateStaticField字段的值将被设置为100
     *     }
     * }
     * }</pre>
     */
    @Deprecated
    public abstract void howToUseAccessor();
}
