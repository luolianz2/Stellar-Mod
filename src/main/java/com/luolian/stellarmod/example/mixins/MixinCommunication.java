package com.luolian.stellarmod.example.mixins;

public abstract class MixinCommunication {
    /**
     * <h1>mixin之间的通信</h1>
     * <h2>这是在说什么？</h2>
     * 当你尝试使用mixin为目标类添加字段或方法，并且希望在其它类甚至其它mixin中访问这些字段或方法时，
     * 你会发现你无法直接访问它们，因为它们在编译期属于mixin类，而不是目标类。<br>
     * 这时我们需要使用接口来实现mixin之间的通信。<br>
     * 术语和概念没那么重要，直接来看例子：
     * <pre>{@code
     * @Mixin(Player.class)
     * public class PlayerMixin {
     *     // 在目标类中添加的字段
     *     @Unique
     *     private int modid$myField;
     * }
     * }</pre>
     * 假设我们想要在以下类中访问这个字段：
     * <pre>{@code
     * public class SomeOtherClass {
     *     public void someMethod(Player player) {
     *         // 直接访问myField会报错，因为它在编译期属于PlayerMixin类，而不是Player类
     *         // int value = player.myField; // 这行代码会报错
     *     }
     * }
     * }</pre>
     * <h2>如何做？</h2>
     * 我们需要定义一个接口来暴露这个字段或方法，然后让mixin实现这个接口。<br>
     * 注意，这里存在一个细节，Mixin类实现接口意味着目标类也会实现这个接口。<br>
     * 比如这样定义接口：
     * <pre>{@code
     * public interface MyFieldController {
     *     int modid$getMyField();
     *     void modid$setMyField(int value);
     * }
     * }</pre>
     * 然后使PlayerMixin实现这个接口：
     * <pre>{@code
     * @Mixin(Player.class)
     * public class PlayerMixin implements MyFieldController {
     *     @Unique
     *     private int myField;
     *
     *     @Unique @Override
     *     public int modid$getMyField() {
     *         return myField;
     *     }
     *
     *     @Unique @Override
     *     public void modid$setMyField(int value) {
     *         this.myField = value;
     *     }
     * }
     * }</pre>
     * 这样操作之后，我们就可以在SomeOtherClass中通过强转来访问这个字段了：
     * <pre>{@code
     * public class SomeOtherClass {
     *     public static void someMethod(Player player) {
     *         // 可以通过强转来访问，但使用模式变量可以避免一些神秘的BUG
     *         if (player instanceof MyFieldController controller) {
     *             int value = controller.modid$getMyField();
     *         }
     *     }
     * }
     * }</pre>
     */
    @Deprecated
    public abstract void howToCommunicate();
}
