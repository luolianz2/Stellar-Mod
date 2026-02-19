package com.luolian.stellarmod.example.java;

public abstract class FunctionalInterfaceOperation {
    /**
     * <h1>函数式接口和lambda表达式</h1>
     * <h2>什么是函数式接口？</h2>
     * 函数式接口是指仅包含一个抽象方法的接口。<br>
     * 这种接口可以被隐式转换为lambda表达式或方法引用。<br>
     * 使用函数式接口可以使代码更简洁和可读，特别是在需要传递某种特定代码的行为时。<br>
     * 以下是一个函数式接口的演示：
     * <pre>{@code
     * @FunctionalInterface
     * public interface ExampleFunctionalInterface {
     *     // 函数式接口是一个接口
     *     // 其中只有一个抽象方法
     *     // 或者一些default方法和static方法
     *     int doSomething();
     * }
     * }</pre>
     * 函数式接口可以使用{@code @FunctionalInterface}注解来标记，不过不是强制的。
     * <h2>如何使用？</h2>
     * 函数式接口可以通过lambda表达式或方法引用来干净利落地实现。<br>
     * 先演示一下不使用这些高级语法时的情况：
     * <pre>{@code
     * public void exampleMethod() {
     *     // 假设我们需要在这个方法中调用this.test()方法
     *     // 实现时我们需要使用匿名内部类来实现Runnable接口
     *     this.test(new Runnable() {
     *         @Override
     *         public void run() {
     *             // doSomething here
     *             System.out.println("Hello, World!");
     *         }
     *     });
     * }
     *
     * private void test(Runnable runnable) {
     *     // 使用Runnable接口来指定一个行为
     *     runnable.run();
     * }
     * }</pre>
     * 可以看到，简直太难看了！
     * 使用lambda表达式后，我们可以大幅简化代码：
     * <pre>{@code
     * public void exampleMethod() {
     *     // 使用lambda表达式来实现Runnable接口
     *     this.test(() -> {
     *         // doSomething here
     *         System.out.println("Hello, World!");
     *     });
     * }
     * }</pre>
     * 这样以来代码就变得非常简洁了！<br>
     * 不过你可能会疑惑，实参中的括号是什么？{@code ->}又是什么？<br>
     * <ul>
     *     <li>{@code ->}是lambda表达式的语法，它负责让编译器知道这是一个lambda表达式，以下我们简称其为“箭头”。</li>
     *     <li>箭头左侧是所需函数式接口的形参，右侧是表达式或代码块，用于实现你自己的逻辑。</li>
     * </ul>
     * 在这个例子中，{@code Runnable}接口的抽象方法是{@code run()}，它没有形参和返回值，
     * 所以箭头左侧是空的括号，编译器也不会警告{@code this.test()方法的返回值已忽略}。<br>
     * 下面我们演示一下其它函数式接口：
     * <pre>{@code
     * public void exampleMethod() {
     *     // 使用lambda表达式来实现Consumer接口
     *     this.test((String s) -> {
     *         // doSomething here
     *         System.out.println(s);
     *     });
     * }
     *
     * private void test(Consumer<String> consumer) {
     *     // 使用Consumer接口来指定一个行为
     *     consumer.accept("Hello, World!");
     * }
     * }</pre>
     * 在这个例子中，{@code Consumer}接口的抽象方法是{@code accept(T t)}，它有一个形参但没有返回值，
     * 箭头左侧的{@code String s}就是{@code accept(T t)}方法的形参，箭头右侧的代码块就是{@code accept(T t)}方法的实现。<br>
     * 这里调用的具体含义是：用{@code System.out.println()}方法来对传入的字符串进行处理，即{@code test()}方法中的{@code "Hello, World!"}字符串。<br>
     * <br>
     * 不过这里其实还有可以简化的地方，我们可以省略形参的类型，
     * 因为编译器可以通过上下文推断出它的类型，也就是lambda表达式的最终形态：
     * <pre>{@code
     * public void exampleMethod() {
     *     // 使用lambda表达式来实现Consumer接口
     *     this.test(s -> {
     *         // doSomething here
     *         System.out.println(s);
     *     });
     * }
     * }</pre>
     * 在main方法中调用这个exampleMethod方法，你会看到它输出了{@code Hello, World!}字符串。<br>
     */
    @Deprecated
    public abstract void functionalInterfaceConcept();

    /**
     * <h1>方法引用</h1>
     * 方法引用是lambda表达式更进一步简化的形式，它允许我们直接引用一个方法来实现函数式接口。<br>
     * 方法引用的语法是{@code ClassName::methodName}，具体语法看起来像这样：
     * <ul>
     *     <li>{@code ClassName::StaticMethodName}，即“类名::静态方法名”，其表示把形参传入你所引用的方法中。</li>
     *     <li>{@code instance::InstanceMethodName}，即“实例名::实例方法名”，其表示把形参传入由指定实例调用的方法中。</li>
     *     <li>{@code ClassName::InstanceMethodName}，即“类名::实例方法名”，其表示让形参成为实例方法的调用者调用你所引用的方法。</li>
     *     <li>{@code ClassName::new}，即“类名::new”，其表示把形参传入你所引用的构造方法中。</li>
     * </ul>
     * 方法引用可以让代码更加简洁和可读，只是理解起来有一定门槛。<br>
     * 上面的四种语法中，第2种和第3种语法非常容易混淆，下面我们通过一个例子来演示一下它们的区别：
     * <pre>{@code
     * private List<String> exampleList = Arrays.asList("Hello", "World", "Java");
     * private String exampleString = "Hello, World!";
     * public void exampleMethod() {
     *     // 类名::实例方法名的语法
     *     int totalLength = this.testFunction(String::length);
     *     // 相当于 exampleString -> exampleString.length()
     *     // 语义理解就是：使用String.length方法来处理exampleString字符串，即得到它的长度
     *     System.out.println(totalLength);
     *
     *     // 实例::实例方法名的语法
     *     this.testConsumer(exampleList::add);
     *     // 相当于 s -> exampleList.add(s)
     *     // 语义理解就是：使用exampleList实例的add方法来处理形参，即把它添加到exampleList列表中
     *     System.out.println(exampleList);
     * }
     *
     * private int testFunction(Function<String, Integer> function) {
     *     // 使用function形参处理exampleString变量，并返回一个整数
     *     return function.apply(exampleString);
     * }
     *
     * private void testConsumer(Consumer<String> consumer) {
     *     // 使用consumer形参处理一个字符串
     *     consumer.accept("100");
     * }
     * }</pre>
     * 在main方法中调用这个exampleMethod方法，你会看到它输出了{@code 13}和{@code [Hello, World, Java, 100]}。
     */
    @Deprecated
    public abstract void methodReferenceConcept();
}
