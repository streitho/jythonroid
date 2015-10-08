移植关键点：


1,移植使用了一个比较差劲的方法：Jython会动态构造java字节码流，通过将dx.jar导入到classpath,使用其中的方法来将这个java字节码流翻译成dalvik字节码。又

由于dalvik只支持动态载入apk文件中的classes.dex文件，所以需要将生成的dalvik字节码流保存在classes.dex文件，再使用zip打包到一个.apk文件中。

2,dalvik的反射机制实现得不完全，例如Class.getDeclaringClass()就没有实现，不过可以通过内部类的类文件命名规则：类$内部类.class来使用Class.forName()动

态载入

3,运行时需要使用dalvikvm，而不能通过oncreate方法作为入口，因为oncreate方法为入口的是界面，使用了特有的ClassLoader，这个ClassLoader对反射的支持很差

，容易找不到类名

4,实现界面使用了socket来连接后台和前台，但在android模拟器中需要通过redir add (pcport):(emulatorport)来将模拟器端口绑定到pc端口，否则ServerSocket无

法构造。

5,交互式shell比较难实现，因为android中提供的文本编辑控件EditText不能定义光标位置，希望下个版本能解决。