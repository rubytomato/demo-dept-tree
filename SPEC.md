# メソッド依存関係ツリー出力プログラムの開発

## 要件定義

* プロジェクト名: `demo-dept-tree`
* Javaプログラムのメソッド呼び出しを追跡しテキストツリーで出力するプログラム

### アーキテクチャー

* 開発言語: OpenJDK8
* ビルドツール: Gradle
  * 実行可能形式のjarファイルとしてビルドする
* バイトコード解析ライブラリ: ASM
* 引数チェック: picocli
* ロギング: Log4J2

### CLI

* 実行可能形式のjarとしてビルドする

* 呼び出し例

  ```text
  java -jar dept-tree.jar --analyze ./target/classes/**/*.class --analyze ./libs/*.jar --root "com.example.Hoge" --depth 20
  ```

#### 引数の説明

##### `--analyze`

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

##### `--root`

* 必須の引数
  * 省略された場合異常終了する
* 解析する起点となるクラス
  * 読み込んだバイトコードの中から解析する起点となるクラスのFQDN
  * 起点となるクラスに複数のpublic,protected,package-privateメソッドが存在する場合、それぞれについて解析する
  * 複数指定はできない
  * `--analyze` で読み込んだバイトコード内に存在しない場合、処理を中断する

##### `--depth`

* 任意の引数、省略された場合デフォルト値を適用
* rootの深度は1とする
* 解析する深さ
  * デフォルト値: 20
  * 最小値: 1
  * 最大値: 100

#### `--help`

* アプリケーションの実行方法についてヘルプテキストをinfoレベルでログに出力する
  * 引数の種類とその説明
  * `--help` が指定された場合は、ヘルプテキスト出力後にアプリケーションは正常終了する
* log4j2.xmlの設定でログはコンソールにも出力する設定のため、ヘルプテキストをログ出力しても利用者の利便性に影響はない

### 機能

* `--root` で指定するクラスのpublic/protected/package-privateメソッドを解析の起点とする
* `--analyze` で指定するclass/jarファイルのメソッドの呼び出し、またメソッド内で生成したオブジェクトのメソッド呼び出しを追跡し結果をツリー形式でログに出力する
  * 追跡先のメソッドのスコープは不問
  * ただし、後述する "除外するパッケージ"のクラスは追跡しない
* interfaceのメソッド呼び出しは、そのinterfaceを実装した具象クラスを追跡する
* リフレクション、ラムダ、ネイティブ呼び出しは検討しない
* メソッドの出現順でツリー化する
  * 分岐条件はすべての条件でメソッド呼び出しをツリー化する
* ツリーはログに出力
  * ログは仕様定義のログ出力で設計

#### 出力例

**ソースコード**

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
```

##### 起点となるクラスに複数のpublic,protected,package-privateメソッドが存在する場合

* 空行3つで区切る
* 例 (com.example.Hoge)

```text
com.example.Hoge#method1(String, String)
 |
 `--- ツリー情報 (省略)



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


#### 除外するパッケージ

* `-a/--analyze` で指定する解析対象から除外するパッケージについて、Java標準API および `org`, `net`, `io` から始まるパッケージを解析の対象外とする
* 除外パッケージのメソッド呼び出しはツリーに表示しない

**Java標準API**

* `java.*`, `javax.*`
* `sun.*`, `com.sun.*`, `jdk.*` も除外する

**org,net,ioから始まるパッケージ**

* `org.*`
* `net.*`
* `io.*`

#### 異常終了時の挙動

* エラーメッセージをerrorレベルでログへ出力する
* 終了コードは `1`

#### 未解決の呼び出し

* `--analyze` に渡されなかったクラスへの呼び出し
* `--analyze` に渡されたが解析できなかったクラス（難読化等）
* 未解決の呼び出しは `[UNRESOLVED]` と表示する

#### 例外処理部内での呼び出し

* 例外を捕捉するcatchブロック内のメソッド呼び出しは `[EXCEPTION]` と表示する
* 例外と例外以外の両方で同じメソッドを呼び出している場合は、ツリーにそれぞれ出力する
* 例
  ```text
  `--- com.example.Hoge#method1(String)
  |
  `--- [EXCEPTION] com.example.Hoge#method1(String)
  ```

#### 循環参照時のツリー表示方法

* 循環参照時の呼び出しは `[CIRCULAR]` と表示する
* 例
  ```text
  `--- [CIRCULAR] com.example.Hoge#method1(String)
  ```

#### --depth 上限到達時のツリー表示方法

* `--depth`で指定する上限に到達した呼び出しは `[MAX]` のみ表示する
* 上限到達時は、そのルートはそれ以上解析しない
* 例
  ```text
  `--- [MAX]
  ```

 ##### `[MAX]` が複数並ぶ場合の可読性

* 以下のように `[MAX]` が複数並ぶことを許容する
* 例
 ```text
  `--- [MAX]
  |
  `--- [MAX]
  |
  `--- [MAX]
 ```

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
| 解析対象コード  | -a | --analyze   | 必須 | 解析するclass、jarファイルのファイルパス | 可   | なし  |
| 解析開始クラス  | -r | --root      | 必須 | 解析するクラスのFQDN            | 不可 | なし  |
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
* #3) 解析開始クラスにpublic/protected/package-privateスコープのメソッドがあるかチェックする
  * 見つかったメソッド名をログに出力
* #4) 解析開始クラスに存在するpublic/protected/package-privateスコープのメソッドを起点にしてメソッドの呼び出しを解析しメソッド呼び出しのツリー情報を生成する
* #5) 生成した呼び出しのツリーの情報をログへ出力する

### ユニットテストコード

* 実装不要


### コード最適化

* Mainクラスに実装を集中するのではなく、適切なクラス分割を行う


### 検証用コード

* `com.example.depttree.samples`, `com.example.depttree.other.samples` パッケージ以下のクラスはGitHub Copilotによる検証およびコード修正の対象外とし、このSPEC.MDに書かれた要件や仕様の定義から除外する
