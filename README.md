# demo

Java 21 / Spring Boot 4.1.1 の Gradle（Groovy DSL）マルチモジュールプロジェクトです。

## モジュール構成

- `common`: 共通ライブラリ
- `core`: ドメイン／コアライブラリ（`common` に依存）
- `repository`: MyBatis リポジトリライブラリ（`core`, `common` に依存、MySQL を使用）
- `service`: サービスライブラリ（`core`, `repository`, `common` に依存）
- `web`: Spring MVC ウェブアプリケーション（`service` に依存）
- `batch`: Spring Batch アプリケーション（`service` に依存、MySQL を使用）

## ビルドと起動

JDK 21 をインストールし、`JAVA_HOME` にそのインストール先を設定してください。
Gradle Wrapper（Gradle 9.8.1）を同梱しているため、Gradle の別途インストールは不要です。
初回実行時は Gradle と依存ライブラリのダウンロードにインターネット接続が必要です。

プロジェクトルートで実行します（Windows PowerShell）。

```powershell
.\gradlew.bat clean build
java -jar web\build\libs\web-0.0.1-SNAPSHOT.jar
java -jar batch\build\libs\batch-0.0.1-SNAPSHOT.jar
```

macOS / Linux では `./gradlew` を使用します。

```shell
./gradlew clean build
java -jar web/build/libs/web-0.0.1-SNAPSHOT.jar
java -jar batch/build/libs/batch-0.0.1-SNAPSHOT.jar
```

`build` は全モジュールのコンパイル、テスト、JAR 生成を実行します。
全モジュールで JUnit Jupiter のテストを作成できます。
`service` に DI テスト、`repository` に MyBatis の構成・SQL テストを用意しています。
`web` と `batch` は実行可能な Spring Boot JAR、その他のモジュールは通常のライブラリ JAR を生成します。
開発時は `.\gradlew.bat :web:bootRun` または `.\gradlew.bat :batch:bootRun` でも起動できます。

## 依存関係のバージョン管理

バージョンはルートの `build.gradle` で管理します。

- Spring Boot プラグイン: `plugins` ブロックで指定します。
- Spring Framework / MySQL Connector/J など: 全モジュールに適用する Spring Boot BOM で管理します。
- MyBatis Starter（テスト用を含む）/ Generator: `ext` の `mybatisSpringBootVersion` / `mybatisGeneratorVersion` で指定し、子モジュールから参照します。

`mybatisGenerator` は独立した構成のため、Spring Boot BOM を別途適用しています。
バージョンの更新はルートで行い、子モジュールに個別のバージョンを直接指定しない運用とします。
これは指定箇所の集約であり、異なるバージョンの指定を強制的に禁止する設定ではありません。

## Lombok / JUnit とソース構成

全モジュールで Lombok をメインソース・テストソースの両方に使用できます。
Lombok はコンパイル専用とし、それぞれのアノテーションプロセッサも設定しています。
JUnit Jupiter（JUnit Platform）とテスト実行用 Launcher もルートの `build.gradle` で共通設定しています。
これらのバージョンは Spring Boot BOM で管理します。

各モジュールには、以下のソースフォルダを用意しています。
`<module>` は `common` / `core` / `repository` / `service` / `web` / `batch` です。

```text
<module>/
  src/main/java/com/example/demo/<module>/
  src/main/resources/
  src/test/java/com/example/demo/<module>/
  src/test/resources/
```

通常の単体テストは Spring コンテキストや DB 接続なしで実装できます。
`service` には `spring-boot-starter-test`、
`repository` には `mybatis-spring-boot-starter-test` をテスト専用の依存関係として追加しています。

```powershell
.\gradlew.bat test
.\gradlew.bat :core:test
```

## MyBatis / MySQL

Spring Boot 4 対応の MyBatis Spring Boot Starter 4.0.0 と MySQL Connector/J を使用します。
`web` と `batch` は `com.example.demo.repository.mapper` をスキャンし、
`repository` の `src/main/resources/mapper` に生成される XML を読み込みます。

両アプリケーションの `src/main/resources/application.yml` には、以下のダミー接続先を設定しています。

| 設定 | ダミー値 | 実行時の上書き用環境変数 |
| --- | --- | --- |
| URL | `jdbc:mysql://localhost:3306/demo_dummy` | `DB_URL` |
| ユーザー名 | `dummy_user` | `DB_USERNAME` |
| パスワード | `dummy_password` | `DB_PASSWORD` |

これらは実在する接続先や認証情報ではありません。DB を使用するには実際の接続情報に変更してください。
Spring Batch を実行する環境では、Spring Batch 用の MySQL メタデータテーブルも事前に用意してください。
H2 によるインメモリ DB は使用しません。

### repository / service の DB テスト設定

両モジュールの `src/test/resources/application.yml` に、テスト用の MySQL 接続設定を用意しています。
テストリソースにのみ配置するため、通常のライブラリ JAR には含まれません。

| 設定 | ダミー値 | テスト時の上書き用環境変数 |
| --- | --- | --- |
| URL | `jdbc:mysql://localhost:3306/demo_test_dummy` | `TEST_DB_URL` |
| ユーザー名 | `dummy_test_user` | `TEST_DB_USERNAME` |
| パスワード | `dummy_test_password` | `TEST_DB_PASSWORD` |

DB テストを実行する際は、テスト専用の MySQL データベース・対象テーブルと接続情報を用意してください。
通常実行用の `DB_URL` などとは分けていますが、Spring 標準の `SPRING_DATASOURCE_URL` などの設定でも上書きされるため、
テスト環境に本番の接続情報を渡さないでください。
MyBatis の Mapper XML 読み込み設定も含めています。

このファイルは Spring Boot の設定読み込みを使用するテスト向けです。
JUnit 単体や通常の `@ContextConfiguration` だけでは自動的に読み込まれません。
両モジュールにはテスト専用の起動構成クラスも用意しています。
これらは `src/test` にのみ存在し、実行用 JAR には入りません。
テーブルの自動作成は行いません。

#### service: Spring Boot Test と DI

`ServiceTestApplication` を起点に、`service` / `core` のコンポーネントと
`com.example.demo.repository.mapper` の Mapper を登録します。
`ServiceDependencyInjectionTest` は `@SpringBootTest` と `@Autowired` を使い、
テスト専用 Bean のコンストラクタ DI を検証します。HTTP サーバーは起動しません。
このサンプルでは SQL を実行しないため、DB は不要です。

実際のサービスも同じ構成で注入できます。DB を使用するテストにはテスト用の接続情報を設定してください。
他のパッケージの構成が必要な場合は、テストで `@Import` するかスキャン範囲を明示してください。

#### repository: MyBatis Test

`RepositoryTestApplication` で生成 Mapper と自作 Mapper の共通パッケージ
`com.example.demo.repository.mapper` をスキャンします。
自作 Mapper の XML は `src/main/resources/mapper`、テスト専用 XML は
`src/test/resources/mapper` に配置すると読み込まれます。
`@MybatisTest` は通常のサービス Bean を登録しないため、必要な DAO などは `@Import` してください。

- `MybatisConfigurationTest`: DB に接続せず、Mapper の注入とアノテーション / XML の SQL 登録を確認します。
  DB への接続を開始するテスト用トランザクションは `Propagation.NOT_SUPPORTED` で無効化しています。
- `MybatisSqlTest`: MySQL に接続し、アノテーション SQL と XML SQL を実行します。
  サンプルはテーブル不要の `SELECT` です。`TEST_DB_URL` が未設定の場合は、このクラスのみスキップします。
  設定済みで接続に失敗した場合は、スキップではなく失敗になります。

いずれも `@AutoConfigureTestDatabase(replace = Replace.NONE)` を指定し、
組み込み DB への置き換えを禁止しています。
SQL テストには `@MybatisTest` のトランザクションと終了時のロールバックが適用されます。
独自の SQL テストではテストデータを用意し、MySQL の DDL などロールバックできない処理は明示的に後片付けしてください。

```powershell
# DB 不要の DI / MyBatis 構成テスト（SQL テストはスキップ）
.\gradlew.bat :service:test :repository:test

# 以下の値は例です。実際のテスト専用 MySQL の接続情報に置き換えてください。
$env:TEST_DB_URL = 'jdbc:mysql://localhost:3306/demo_test_dummy'
$env:TEST_DB_USERNAME = 'dummy_test_user'
$env:TEST_DB_PASSWORD = 'dummy_test_password'
.\gradlew.bat :repository:test --tests '*MybatisSqlTest' --rerun-tasks
```

接続設定の変更後は、Gradle の前回のテスト結果を再利用しないよう `--rerun-tasks` を指定してください。

### コード自動生成

MyBatis Generator 1.4.2 の設定ファイルは `repository/generateConfig.xml` です。
`jdbcConnection` のダミー接続情報と `table` のサンプルテーブル名 `sample` を、
実際の MySQL 接続先と対象テーブルに変更してから、プロジェクトルートで実行します。
生成時の接続設定はこの XML で管理し、アプリケーション用の環境変数とは連動しません。
実際の認証情報をリポジトリにコミットしないでください。

```powershell
.\gradlew.bat :repository:mybatisGenerate
.\gradlew.bat build
```

| 生成物 | 出力先（`repository` 内） |
| --- | --- |
| モデル / Example | `src/main/java/com/example/demo/repository/model` |
| Mapper インターフェース | `src/main/java/com/example/demo/repository/mapper` |
| Mapper XML | `src/main/resources/mapper` |

生成には対象テーブルが存在する MySQL への接続が必要です。
通常の `build` では生成タスクを実行しないため、ビルド時の DB 接続は不要です。
再生成時は既存の Java / XML 生成ファイルを上書きするため、生成ファイルは直接編集しないでください。
