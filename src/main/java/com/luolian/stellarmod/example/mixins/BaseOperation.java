package com.luolian.stellarmod.example.mixins;

public abstract class BaseOperation {
    /**
     * <h1>如何在mixin中实用this关键字</h1>
     * <h2>获取this实例</h2>
     * 在mixin中，单独使用this关键字会指向mixin类的实例，而不是被混入的目标类的实例。<br>
     * 如果你想获取被混入的目标类的实例，你需要使用强转来将this转换为目标类的类型。<br>
     * 直接这样写就行了：
     * <pre>{@code
     * @Mixin(TargetClass.class)
     * public class TargetClassMixin {
     *     @Inject(method = "targetMethod", at = @At("HEAD"))
     *     private void injectMethod(CallbackInfo ci) {
     *         // 使用强转将this转换为目标类的类型
     *         // 这里的强转是安全的，不必担心
     *         TargetClass self = (TargetClass) (Object) this;
     *         // 一般获取到的实例会被命名为“self”，与“this”释义相似
     *     }
     * }
     * }</pre>
     * <h2>使用实例方法</h2>
     * 虽然你可以通过强转获取目标类的实例，但你不能直接在mixin中调用目标类的实例方法。<br>
     * 这是因为mixin类和目标类是不同的类，编译器无法确定你调用的方法是哪个类的方法。<br>
     * 如果你想调用目标类的实例方法，你需要使用{@code @Shadow}注解来声明一个与目标类中方法签名相同的抽象方法。<br>
     * 就像这样：
     * <pre>{@code
     * @Mixin(TargetClass.class)
     * public class TargetClassMixin {
     *     @Shadow
     *     private abstract void targetMethod();
     *
     *     @Inject(method = "targetMethod", at = @At("HEAD"))
     *     private void injectMethod(CallbackInfo ci) {
     *         // 直接调用目标类中的实例方法
     *         this.targetMethod();
     *     }
     * }
     * }</pre>
     */
    @Deprecated
    public abstract void getThis();

    /**
     * <h1>如何使用mixin在目标类中添加内容</h1>
     * mixin提供了{@code @Unique}注解来为目标类添加新的字段或方法。<br>
     * 只需要这样写：
     * <pre>{@code
     * @Mixin(TargetClass.class)
     * public class TargetClassMixin {
     *     @Unique
     *     private int someNewField;
     *
     *     @Unique
     *     private void someNewMethod() {
     *         // 在这里实现你想要添加的方法
     *     }
     * }
     * }</pre>
     * 但是需要注意的是，使用{@code @Unique}注解添加的字段或方法在编译期属于mixin类，而不是目标类。<br>
     * 想要访问它们，请看{@linkplain MixinCommunication#howToCommunicate()}的javadoc。
     */
    @Deprecated
    public abstract void addNewContent();

    /**
     * <h1>使用mixin注入构造器</h1>
     * 在部分mixin注解中，可以给method参数传入{@code <init>}字符串来注入构造器。<br>
     * 也就是将注入普通方法时的方法名改成{@code <init>}，其它内容基本一致：
     * <pre>{@code
     * @Mixin(TargetClass.class)
     * public class TargetClassMixin {
     *     @Inject(method = "<init>", at = @At("HEAD"))
     *     private void injectConstructor(CallbackInfo ci) {
     *         // 在无参构造器调用时会执行这里的代码
     *     }
     * }
     * }</pre>
     */
    @Deprecated
    public abstract void constructorMixin();

    /**
     * <h1>mixin的最佳实践</h1>
     * <h2>尽量避免使用{@code @Redirect}</h2>
     * 该注解用于重定向方法调用或字段访问，使用该注解会导致被重定向方法在性能分析器中消失，
     * 因此两个重定向作用于同一个方法时会导致性能分析器崩溃。<br>
     * 替代品时MixinExtras提供的{@code @WrapOperation}注解，其中的original.call()方法
     * 会保留被重定向方法在性能分析器中的可见性。<br>
     * <h2>{@code method}参数在前，{@code at}参数在后</h2>
     * 代码的可读性很重要，{@code method}参数在前可以让我们更快或方便地注入的目标。
     * 另外当注解参数较多时，适当换行可以提高可读性。
     * <h2>mixin方法的形参名</h2>
     * 虽然形参名不影响代码的功能，但在社区规范中：
     * <ul>
     *     <li>{@code CallbackInfo}的形参名应该是{@code ci}。</li>
     *     <li>{@code CallbackInfoReturnable}的形参名应该是{@code cir}。</li>
     *     <li>{@code @Local}注解的形参名应该与目标方法中的局部变量名相同。</li>
     * </ul>
     * <h2>别忘了给mixin方法起一个好名字</h2>
     * 一个语义化的名字可以让我们更方便地了解这个方法做了什么，而不是急头白脸地看方法体，
     * 当然还有一个更底层的原因，就是方法名一般不能重复。
     * <h2>包结构和类名</h2>
     * <ul>
     *     <li>我们可以随意在mixin包下创建新的包，以优化项目结构，尤其在mixin内容较多时可以极大提高可读性。</li>
     *     <li>Mixin类的类名一般以“目标类名+Mixin”来命名，比如{@code PlayerMixin}、{@code EntityMixin}等，
     *     当然这是一般情况，当mixin内容较多时语义化的类名比这个格式更好。</li>
     *     <li>访问器接口的存放的最佳实践是在mixin包下新建一个accessor包。</li>
     * </ul>
     */
    @Deprecated
    public abstract void bestPractices();
}
