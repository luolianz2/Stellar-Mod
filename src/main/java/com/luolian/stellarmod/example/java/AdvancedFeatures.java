package com.luolian.stellarmod.example.java;

public abstract class AdvancedFeatures {
    /**
     * <h1>模式变量（Pattern Variables）</h1>
     * <h2>概述</h2>
     * 你不用管模式变量定义是啥，你只需要知道这个东西可以让你少写一行强制类型转换就行了。
     * <h2>示例</h2>
     * 先看不使用模式变量的例子：
     * <pre>{@code
     * Object obj = "Hello, World!";
     * if (obj instanceof String) {
     *     String str = (String) obj; // 需要强制类型转换
     *     System.out.println(str.length());
     * }
     * }</pre>
     * 当我们使用了模式变量：
     * <pre>{@code
     * Object obj = "Hello, World!";
     * if (obj instanceof String str) {
     *     // 这里的“str”就是一个模式变量
     *     // 因为instanceof表达式确保了“obj”变量在if语句内是String类型
     *     System.out.println(str.length());
     * }
     * }</pre>
     * <h2>注意事项</h2>
     * 就像逻辑概念一样，模式变量只能在它们被定义时的作用域内使用。<br>
     * 上面的例子中，模式变量“str”只能在if语句块内使用，
     * 如果你想在if语句块外使用，你可以反转条件并结束方法（return或throw）：
     * <pre>{@code
     * Object obj = "Hello, World!";
     * if (!(obj instanceof String str)) {
     *     // 这里的“str”是不可用的
     *     return;
     * }
     * // 这里的“str”是可用的
     * // 因为上面的代码确保了“obj”变量在if语句外是String类型
     * System.out.println(str);
     * }</pre>
     */
    @Deprecated
    public abstract void patternVariables();

    /**
     * <h1>Switch表达式（Switch Expressions）</h1>
     * <h2>概述</h2>
     * Switch语句写起来比较废话和麻烦，有了此高级语法，Switch语句可以变得更简洁和强大。
     * <h2>示例</h2>
     * 假设我们有这样一个枚举类：
     * <pre>{@code
     * public enum Day {
     *     MONDAY, TUESDAY, WEDNESDAY,
     *     THURSDAY, FRIDAY, SATURDAY, SUNDAY
     *     ;
     * }
     * }</pre>
     * 先演示一下传统Switch语句：
     * <pre>{@code
     * Day day = Day.MONDAY;
     * String typeOfDay;
     * switch (day) {
     *     case MONDAY:
     *     case TUESDAY:
     *     case WEDNESDAY:
     *     case THURSDAY:
     *     case FRIDAY:
     *         typeOfDay = "Weekday";
     *         break;
     *     case SATURDAY:
     *     case SUNDAY:
     *         typeOfDay = "Weekend";
     *         break;
     *     default:
     *         typeOfDay = "Unknown";
     * }
     * }</pre>
     * 可以看到，传统的switch语句需要写很多case标签和break语句，非常麻烦。<br>
     * 使用Switch表达式，我们可以这样写：
     * <pre>{@code
     * Day day = Day.MONDAY;
     * String typeOfDay;
     * switch (day) {
     *     case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> typeOfDay = "Weekday";
     *     case SATURDAY, SUNDAY -> typeOfDay = "Weekend";
     *     default -> typeOfDay = "Unknown";
     * };
     * }</pre>
     * Switch表达式使用箭头（->）来分隔case标签和对应的代码块（或表达式），
     * 并且允许我们在同一个case中列出多个标签，大大简化了代码。<br>
     * 另外，Switch表达式还可以直接返回一个值，可以进一步缩短代码：
     * <pre>{@code
     * Day day = Day.MONDAY;
     * String typeOfDay = switch (day) {
     *     case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> "Weekday";
     *     case SATURDAY, SUNDAY -> "Weekend";
     *     default -> "Unknown";
     * };
     * }</pre>
     * 当你需要在case标签中执行多行代码时，你可以使用大括号来定义一个代码块，
     * 并使用yield关键字结束代码块并返回一个值（return关键字在Switch表达式中是非法的）：
     * <pre>{@code
     * Day day = Day.MONDAY;
     * String typeOfDay = switch (day) {
     *     case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> {
     *         System.out.println("It's a weekday!");
     *         yield "Weekday"; // 使用yield关键字返回值
     *     }
     *     case SATURDAY, SUNDAY -> {
     *         System.out.println("It's a weekend!");
     *         yield "Weekend"; // 使用yield关键字返回值
     *     }
     *     default -> {
     *         System.out.println("Unknown day!");
     *         yield "Unknown"; // 使用yield关键字返回值
     *     }
     * };
     * }</pre>
     */
    @Deprecated
    public abstract void switchExpressions();

    /**
     * <h1>类型推断（Type Inference）</h1>
     * <h2>概述</h2>
     * 类型自动推断可以让代码更简洁，尤其是在使用泛型时，可以避免冗长的类型声明。<br>
     * 类型推断分为多种情况，最常见的是“泛型类型推断”和“局部变量类型推断”。<br>
     * <h2>泛型类型推断</h2>
     * <pre>{@code
     * List<String> list1 = new ArrayList<String>(); // 没有类型推断，需要重复声明String
     * List<String> list2 = new ArrayList<>(); // 编译器自动推断ArrayList的类型参数为String
     * }</pre>
     * <h2>局部变量类型推断</h2>
     * <pre>{@code
     * var message = "Hello, World!"; // 编译器自动推断变量类型为String
     * var number = 42; // 编译器自动推断变量类型为int
     * var list = new ArrayList<String>(); // 编译器自动推断变量类型为ArrayList<String>
     * var list2 = new ArrayList<>(); // 编译器自动推断变量类型为ArrayList<Object>，但这样写很欠揍
     * }</pre>
     * 需要注意的是：
     * <ul>
     *     <li>顾名思义，局部变量类型推断仅能用于局部变量，比如不能应用于成员变量。</li>
     *     <li>使用var进行局部变量类型推断时，变量必须在声明时初始化，因为编译器需要通过初始值来推断变量的类型。</li>
     *     <li>类型推断虽然可以让代码看起来更简洁，但过度使用会降低代码的可读性。</li>
     * </ul>
     */
    @Deprecated
    public abstract void TypeInference();
}
