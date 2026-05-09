# demo-dept-tree

## 使用例

```sh
java -jar .\build\libs\dept-tree.jar --root "com.example.samples.Hoge" --analyze .\build\classes\java\main\com\example\depttree\*.class --analyze .\build\classes\java\main\com\example\samples\*.class --depth 100
```

結果

```text
# ===========
# root method ( method1 )
# ===========
com.example.samples.Hoge#method1(String, String)
 |
 `--- com.example.samples.Fuga#method1(Long)
 |      |
 |      `--- com.example.samples.Piyo#method1(Integer, Integer, String)
 |      |      |
 |      |      `--- com.example.samples.Peko#method1()
 |      |      |      |
 |      |      |      `--- com.example.samples.Taco#method2()
 |      |      |
 |      |      `--- com.example.samples.Piyo#method2()
 |
 `--- com.example.samples.Fuga#method2(String)
 |
 `--- com.example.samples.Poyo#method3(String)
 |      |
 |      `--- com.example.samples.Poyo#method1()
 |      |
 |      `--- com.example.samples.Taco#method1()


# ===========
# root method ( method2 )
# ===========
com.example.samples.Hoge#method2(Date)
```
