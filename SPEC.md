# メソッド呼び出し関係ツリー出力アプリケーション

## 要件定義

* プロジェクト名: `demo-dept-tree`
* Javaプログラムのメソッド呼び出しを追跡し、その結果をツリー形式で出力するアプリケーション

### アーキテクチャー

* 開発言語: OpenJDK8
* ビルドツール: Gradle
  * 実行可能形式のjarファイルとしてビルドする
* バイトコード解析ライブラリ: ASM
* 引数チェック: picocli
* ロギング: Log4J2

### CLI

* 実行可能形式のjarとしてビルドする

* アプリケーションの実行例

  ```text
  java -jar dept-tree.jar --root "com.example.Hoge" --analyze ./target/classes/**/*.class --analyze ./libs/*.jar --depth 20
  ```

### 機能

* 引数 ( `--root` ) で指定するクラスのpublic/protected/package-privateメソッドを追跡の起点とする
* 引数 ( `--analyze` ) で指定するclassファイル/jarファイルを解析し「メソッドの呼び出し」を追跡し、結果をツリー形式でログに出力する
  * 追跡先のメソッドのスコープは不問
  * ただし、後述する "除外するパッケージ"のクラスのメソッド呼び出しは追跡しない
* interfaceのメソッド呼び出しは、そのinterfaceを実装した具象クラスのメソッドを追跡する
* 抽象メソッドの呼び出しは、その抽象メソッドを実装した具象クラスのメソッドを追跡する
* リフレクション、ラムダ、ネイティブ呼び出しは検討しない
* メソッドの出現順でツリー化する
  * 分岐条件はすべての条件でメソッド呼び出しを追跡する
* メソッド呼び出しツリーはログに出力
  * ログは仕様定義のログ出力で設計
  * ログファイル名は `--root` で指定したクラスのFQDNに `.log` を付与した名前とする
* 引数 ( `--marker` ) で指定するテキストファイルを読み取り、このファイルの記録されたクラスのFQDNがツリーに現れた場合、次の処理を行う
  * 出現回数を数える
  * ツリー出力の末尾に現れた回数をレポートする

### 引数の説明

#### `--root`

* 必須の引数
  * 省略された場合異常終了する
* 解析する起点となるクラス
  * 読み込んだバイトコードの中から解析する起点となるクラスのFQDN
  * 起点となるクラスに複数のpublic,protected,package-privateメソッドが存在する場合、それぞれについて解析する
  * 複数指定はできない
  * `--analyze` で読み込んだバイトコード内に存在しない場合、処理を中断する

#### `--analyze`

* 必須の引数
  * 省略された場合異常終了する
* 複数指定可能とする
* 解析対象とするclass/jarファイルのファイルパス
  * バイトコードの読み込み元
* シングルアスタリスクのグロブ展開を可能とする
* ダブルアスタリスクのグロブ展開を可能とする
  * 例
  ```text
  .\target\classes\**\*.class
  ```
* グロブ展開はツール側の責務
* 同一FQDNが複数ファイルで見つからないものとする

#### `--marker`

* 任意の引数
* 出現回数を数えるクラスのFQDNを記録したテキストファイルのファイルパス
  * このファイルに記録したクラスがメソッド呼び出しツリーに現れた回数を数える
* ツリー出力の最後に出現回数をレポートする
* テキストファイルは以下のフォーマット
  * 1行に1クラスのFQDN
  * 改行コードはLF
  * 例
    ```text
    com.example.Fuga
    com.example.Taco
    ```
* 引数に指定されたファイルが存在しない場合、または空ファイルの場合異常終了とする

#### `--depth`

* 任意の引数、省略された場合デフォルト値を適用
* `--root` で指定したクラスの起点メソッドの深度は1とする
* 解析する深さ
  * デフォルト値: 20
  * 最小値: 1
  * 最大値: 100

#### `--help`

* アプリケーションの実行方法についてヘルプテキストをinfoレベルでログに出力する
  * 引数の種類とその説明
  * `--help` が指定された場合は、ヘルプテキスト出力後にアプリケーションは正常終了する
* log4j2.xmlの設定でログはコンソールにも出力する設定のため、ヘルプテキストをログ出力しても利用者の利便性に影響はない

### 出力例

**サンプルのソースコード**

```java
public class Hoge {
  public void method1(String a, String b) {
    Fuga fuga = new Fuga();
    fuga.method1(100L);
    fuga.method2(a + b);
    Poyo poyo = new Poyo();
    poyo.method3(a + b);
  }

  void method2(Date date) {
      System.out.println("method2");
  }
}

public class Fuga {
  public void method1(Long a) {
    Piyo piyo = new Piyo();
    piyo.method1();
  }
  public void method2(String b) {
    // なにかの処理
  }
}

public interface PSeries {
    void method1();
}

public class Piyo implements PSeries {
  public void method1() {
     Peko peko = new Peko();
     peko.method1();
     method2();
  }
  private void method2() {
    // 何かの処理
  }
}

public class Peko implements PSeries {
  public void method1() {
    Taco taco = new Taco();
    taco.method2();
  }
}

public class Poyo {
  private void method1() {
    // なにかの処理
  }
  public void method3(String b) {
    method1();
    Taco taco = new Taco();
    taco.method1();
    Popo popo = new Popo();
    popo.method1();
  }
}

public class Taco {
  public void method1() {
    // なにかの処理
  }
  public void method2() {
    // なにかの処理
  }
}

public class Popo {
  public void method1() {
    Taco taco = new Taco();
    taco.method2();
  }
}
```

**期待する呼び出し関係の解析結果**

```text
# ===========
# root method ( {起点となるメソッド名} )
# ===========
com.example.Hoge#method1(String, String)
 |
 `--- com.example.Fuga#method1(Long)
 |     |
 |     `--- com.example.Piyo#method1(Integer, Integer, String)
 |           |
 |           `--- com.example.Peko#method1()
 |           |     |
 |           |     `--- com.example.Taco#method2()
 |           |
 |           `--- com.example.Piyo#method2()
 |
 `--- com.example.Fuga#method2(String)
 |
 `--- com.example.Poyo#method3(String)
       |
       `--- com.example.Poyo#method1()
       |
       `--- com.example.Taco#method1()
       |
       `--- [UNRESOLVED] com.example.Popo#method1()



# ==============
# marker classes report
# ==============
* com.example.Fuga (2)
* com.example.Taco (2)
```

#### 起点となるクラスに複数のpublic,protected,package-privateメソッドが存在する場合

* 空行3つで区切る
* 例 (com.example.Hoge)

```text
# ===========
# root method ( method1 )
# ===========
com.example.Hoge#method1(String, String)
 |
 `--- ツリー情報 (省略)



# ===========
# root method ( method2 )
# ===========
com.example.Hoge#method2(Date)
 |
 `--- ツリー情報 (省略)
```

#### 引数の表記

* 単純型名（String, int, Integer 等）で出力する
* 配列型、ジェネリクス型の表示は下表の表1のように出力する

**表1**

| 引数の実際の型 | 出力候補 |
| :------------ | :------ |
| String[]      | String[] |
| int[]         | int[] |
| List<String>  | List |


### メソッド呼び出しの追跡を除外するパッケージ

* `-a/--analyze` で指定する解析対象から除外するパッケージについて、Java標準API および `org`, `net`, `io` から始まるパッケージを解析の対象外とする
* 除外パッケージのメソッド呼び出しはツリーに表示しない

**Java標準API**

* `java.*`, `javax.*`
* `sun.*`, `com.sun.*`, `jdk.*` も除外する

**org,net,ioから始まるパッケージ**

* `org.*`
* `net.*`
* `io.*`

### jarファイルの解析処理の最適化を行う

* `-a/--analyze` で指定する解析対象にjarファイルが存在する場合、jarファイルからclassファイルを抽出して解析対象とする
  * classファイルの抽出先は、カレントディレクトリに作成した一時ディレクトリとする
    * 一時ディレクトリの作成は `Files.createTempDirectory` で行う
  * 一時ディレクトリの中にjarファイル名と同じサブディレクトリを作成し、jarファイル毎に抽出先を分ける

### 異常終了時の挙動

* エラーメッセージをerrorレベルでログへ出力する
* 終了コードは `1`

### 未解決の呼び出し

* `--analyze` に渡されなかったクラスへの呼び出し
* `--analyze` に渡されたが解析できなかったクラス（難読化等）
* 未解決の呼び出しは `[UNRESOLVED]` と表示する

### 例外処理部内での呼び出し

* 例外を捕捉するcatchブロック内のメソッド呼び出しは `[EXCEPTION]` と表示する
* 例外と例外以外の両方で同じメソッドを呼び出している場合は、ツリーにそれぞれ出力する
* 例
  ```text
  `--- com.example.Hoge#method1(String)
  |
  `--- [EXCEPTION] com.example.Hoge#method1(String)
  ```

### 循環参照時のツリー表示方法

* 循環参照時の呼び出しは `[CIRCULAR]` と表示する
* 例
  ```text
  `--- [CIRCULAR] com.example.Hoge#method1(String)
  ```

### --depth 上限到達時のツリー表示方法

* `--depth`で指定する上限に到達した呼び出しは `[MAX]` のみ表示する
* 上限到達時は、そのルートはそれ以上解析しない
* 例
  ```text
  `--- [MAX]
  ```

#### `[MAX]` が複数並ぶ場合の可読性

* 以下のように `[MAX]` が複数並ぶことを許容する
* 例
 ```text
  `--- [MAX]
  |
  `--- [MAX]
  |
  `--- [MAX]
 ```

#### ツリーの最後にクラス出現回数レポートを出力する

* 引数 `--marker` が指定された場合、クラスの出現回数を数えてツリーの最後にレポートを出力する
  * 例
  ```text
  # ==============
  # marker classes report
  # ==============
  * com.example.Fuga (2)
  * com.example.Taco (2)
  ```
* 出現回数が0回の場合、"(0)" という表記は行わない


### 要件の受け入れ基準

* 起点クラス指定でメソッドの呼び出し順のツリーが生成される
* 循環参照で無限ループしない
* 未解決呼び出しが明示される


## 仕様定義

### ソースコード

* 文字コード: UTF-8
* 改行コード: LF
* publicメソッドにはjava doc形式のコメントを必ず書く
* クラスコメントには以下のテンプレートを必ず書く
  ```text
  Generated by GitHub Copilot ({AIモデル名})
  ```

### 引数

* プログラムが受け取る引数の定義は表2のとおり

**表2**

| 引数名         | ショートオプション | ロングオプション | 必須 | 値   | 複数指定  | 初期値 |
| :------------- | :--------------- | :------------- | :--- | :--- | :------- | :------ |
| 解析開始クラス  | -r | --root      | 必須 | 解析するクラスのFQDN            | 不可 | なし  |
| 解析対象コード  | -a | --analyze   | 必須 | 解析するclass、jarファイルのファイルパス | 可   | なし  |
| カウント対象クラス | -m | --marker    | 任意 | 出現回数を数えるクラスのFQDNが記録されたテキストファイルのファイルパス | 不可 | なし |
| 解析深度       | -d | --depth     | 任意 | 1から100の数値                   | 不可 | 20    |
| ヘルプ         | -h | --help      | 任意 | 真偽値型の引数なので値を取らない  | 不可 | false |


### 実装クラス

* 自プロジェクトのクラスは `com.example.depttree` パッケージ以下に実装する


### ログ出力

* ロギングにlog4j2を使用
* ログファイル
  * 文字コード: UTF-8
  * 改行コード: LF
  * ログ出力先、ログファイル名はlog4j2.xmlで定義
  * 出力先: `./dept-tree.log`
  * 毎回上書き (ログファイルがなければ新規作成)
  * アペンダーにコンソール出力とファイル出力を定義するため、ロガーでログを出力するとコンソールとログファイルの両方に出力される
* mainメソッド開始時と終了時にinfoレベルのログを出力する
  * プログラムが受け取った引数の情報を出力する
  * 終了時のログにmainメソッド開始から終了までにかかった時間を出力する
* 自プロジェクトの実装クラスのpublicメソッドの開始時と終了時にinfoレベルのログを出力する
* プログラム実行結果を効率的に解析できるように適切な粒度のdebugレベルのログを出力する
* 例外発生時はerrorレベルのログにstacktraceを出力する


### シーケンス

* #1) アプリケーションが受け取った引数をチェックする
  * 引数の情報をログに出力
  * `--help` が指定された場合アプリケーションを終了する
* #2) 解析開始クラスが解析対象コードに含まれるかチェックする
  * 解析開始クラスが含まれるclassファイル、jarファイルのファイル名をログに出力
* #3) 解析対象ファイルにjarファイルがある場合、jarファイルからclassファイルを一時ディレクトリへ抽出する
  * 以後jarファイルは解析対象から外し、一時ディレクトリに抽出したclassファイルを対象とする
* #4) 解析開始クラスにpublic/protected/package-privateスコープのメソッドがあるかチェックする
  * 見つかったメソッド名をログに出力
* #5) 解析開始クラスに存在するpublic/protected/package-privateスコープのメソッドを起点にしてメソッドの呼び出しを解析しメソッド呼び出しのツリー情報を生成する
* #6) `--marker` が指定されている場合、この引数に指定されたファイルに記録されたクラスのFQDNがツリーに出現する回数を数える
* #7) 生成した呼び出しのツリーの情報をログへ出力する
  * `--marker` が指定されている場合、ツリー情報の後にクラス出現回数のレポートを出力する
  * `--marker` が指定されていない場合、クラス出現回数のレポート出力はスキップする


### ユニットテストコード

* 実装不要


### コード最適化

* Mainクラスに実装を集中するのではなく、適切なクラス分割を行う


### 検証用コード

* `com.example.depttree.samples`, `com.example.depttree.other.samples` パッケージ以下のクラスはGitHub Copilotによる検証およびコード修正の対象外とし、このSPEC.MDに書かれた要件や仕様の定義から除外する
