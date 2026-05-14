# demo-dept-tree

## 使用例

```sh
java -jar .\build\libs\dept-tree.jar --root "com.example.samples.Hoge" --analyze .\build\classes\java\main\com\example\depttree\*.class --analyze .\build\classes\java\main\com\example\samples\*.class --analyze .\build\classes\java\main\com\example\other\exclude\*.class --marker .\marker.txt --exclude .\exclude.txt
```

結果

```text
Default excluded packages: [java., javax., sun., com.sun., jdk., org., io.]
Additional excluded packages: [com.example.other.exclude.]

# ===========
# root method ( method1 )
# ===========
com.example.samples.Hoge#method1(String, String)
 |
 `--- com.example.samples.Fuga#method1(Long)
 |      |
 |      `--- com.example.samples.Piyo#method1()
 |             |
 |             `--- com.example.samples.Peko#method1()
 |             |      |
 |             |      `--- com.example.samples.Taco#method2()
 |             |
 |             `--- com.example.samples.Piyo#method2()
 |
 `--- com.example.samples.Fuga#method2(String)
 |
 `--- [EXCEPTION] [UNRESOLVED] com.example.samples.PException#getMessage()
 |
 `--- [EXCEPTION] com.example.samples.Hoge#method4()
 |
 `--- com.example.samples.Hogo#method3()
 |      |
 |      `--- com.example.samples.Hoke#method1()
 |
 `--- com.example.samples.Poyo#method3(String)
        |
        `--- com.example.samples.Poyo#method1()
        |
        `--- com.example.samples.Taco#method1()
        |
        `--- [UNRESOLVED] com.example.other.samples.Popo#method1()



# ===========
# root method ( method2 )
# ===========
com.example.samples.Hoge#method2(Date)



# ==============
# marker classes report
# ==============
(2) com.example.samples.Fuga
(2) com.example.samples.Taco
```
