package com.luolian.stellarmod.example.mixins.mixin_extras;

public abstract class LocalVariable {
    /**
     * <h1>在mixin方法中获取局部变量</h1>
     * 有时候我们需要在mixin方法中获取目标方法中的局部变量，比如对于以下目标类：
     * <pre>{@code
     * public class TargetClass {
     *     public void targetMethod() {
     *         int localVariable = 42;
     *         // 其它实现
     *     }
     * }
     * }</pre>
     * 如果我们想要在mixin方法中获取这个局部变量，我们需要使用{@code @Local}注解：
     * <pre>{@code
     * @Mixin(TargetClass.class)
     * public class TargetClassMixin {
     *     @Inject(method = "targetMethod", at = @At("..."))
     *     private void injectMethod(CallbackInfo ci, @Local(name = "localVariable") int localVariable) {
     *         // 现在我们可以在这里使用localVariable了
     *     }
     * }
     * }</pre>
     * <h2>注意事项</h2>
     * {@code @Local}注解只能获取局部变量在{@code @At}注解指定的位置时的值，无法定位局部变量的修改。
     * 使用时需要注意{@code @At}注解指定的位置是否在局部变量的作用域内，否则会无法获取到该值。
     */
    @Deprecated
    public abstract void localVariable();
}
