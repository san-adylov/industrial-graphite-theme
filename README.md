# Industrial Graphite — тема для IntelliJ IDEA

Тёплый графит — 90% интерфейса, плоский и спокойный. Сигнальный янтарь, медь и лампочки статуса — 10%, только там, где несут смысл.

## Состав
- **UI-тема** `Industrial Graphite` (New UI, тёмная). Палитра 1:1 из спека:
  редактор `#1C1D1F`, панели `#222326`, заголовки `#26282B`, рама `#1F2023`, разделители `#34363A`,
  текст `#C9C6BF` / `#8C8A85`, янтарь `#D9963A` (акцент, курсор, активная вкладка, статус-бар, прогресс),
  медь `#B87350` (ссылки, поиск), статусы: зелёный `#6FA35A`, красный `#C9524A`, синий `#5B8DB8`.
- **Схема редактора** `Industrial Graphite`: ключевые слова `#D9963A`, классы `#D4C29A`, методы `#8FB3CF`,
  строки `#8FAF6E`, числа `#C98A5E`, аннотации `#B8A05C`, поля/константы `#B6A0C9`, комментарии `#6E6C68`.
  Плюс XML (pom.xml), YAML (application.yml), SQL, консоль и логи Spring Boot (INFO/WARN/ERROR).
- **Иконки Industrial** (векторные SVG 16×16, рисунок не больше 14 px — как у стандартных иконок New UI): class, abstractClass, interface, enum, record, annotation, exception,
  testClass, java, xml, yaml, postgresql, spring, модификаторы private / static / final.
- **Spring**: `@SpringBootApplication` / `@Configuration` → иконка spring;
  бины (`@Component`, `@Service`, `@Repository`, `@Controller`, `@RestController`, репозитории Spring Data…) →
  базовая иконка + бейдж springBean в правом нижнем углу; `@SpringBootTest` и классы `*Test` → testClass.

- **Иконки ресурсов** (спек «Industrial Resources Icons»):
  - корни модуля по настройкам проекта: source root, test root, resources root, test resources root, excluded (только корень исключённой области), модуль;
  - пакеты Java и обычные папки;
  - особые папки ресурсов: `static`/`public`/`assets`, `templates`/`views`, `db/migration`/`changelog`, `i18n`/`messages`/`locales`, `config`;
  - файлы: `.properties`, профили `application-*.yml/properties`, `.json`, `.html`, `.css`, `.js`, картинки, `.env`,
    миграции Flyway (`V1__init.sql`, `R__views.sql`), `logback*.xml`/`log4j2*.xml`, `.txt`.
  - Открытые варианты папок (`*Open.svg`) лежат в плагине, но IntelliJ в дереве проекта не меняет иконку при раскрытии папки, поэтому они не задействованы.

- **Системные иконки** (спек «Industrial System Icons»), подменяются по точным путям из исходников IntelliJ (`*IconMappings.json`):
  - Run / Debug / Stop / Rerun / Build / Rebuild / Coverage / Profile / Pause / Resume, шаги отладчика, брейкпоинты;
  - Search Everywhere, Settings, Refresh, Add, Remove, Delete, Collapse/Expand All, Download Sources;
  - Git: Commit, Push, Update (Pull), History, Diff; Maven: goals, плагины, профили, фазы; узлы БД: схема, таблица, колонка;
  - `.md` и `README*`;
  - панели: Project, Structure, Maven, Gradle, Services, Terminal, Git/Commit, Problems, Run, Debug, Bookmarks,
    Notifications, Find, TODO — 20 px (`@20x20`) и 16 px для компактного режима; Database и Endpoints — через API по ID панели.
- **Конфиги Spring Boot** (спек «Industrial Spring Config Icons»): `application.yml/.yaml/.properties`, `bootstrap.*`,
  профили dev / test / prod в YAML и properties. Синонимы профилей: dev ← `dev`, `local`, `development`;
  test ← `test`, `qa`, `it`, `ci`, `integration`; prod ← `prod`, `production`, `live`, `release`.
  Остальные профили: YAML — значок профиля, properties — базовая спринговая. Обычные `.yml` и `messages.properties` — базовые иконки.
- **Spring Modulith** (спек «Industrial Modulith Icons»):
  - дерево проекта — определяется по тексту файлов, без индексов: модуль = прямой подпакет пакета с `@SpringBootApplication`/`@Modulithic`;
    `sharedModules` → shared, `@ApplicationModule(type = OPEN)` → open; подпакеты модуля: `@NamedInterface` → named interface,
    `api`/`access`/`spi`/`events`/`web` → exposed, остальные → internal. Включается, только если в pom.xml / build.gradle есть
    `spring-modulith` или на классе стоит `@Modulithic`;
  - gutter (module listener, event publication, boundary violation, module test), панель Application Modules и действия
    (dependency, verify, docs) — подменщиком иконок плагина Spring, с вариантами 12/14 и 16/20 px.
- **Иконки gutter** (спек «Industrial Gutter Icons»). Каждая в двух размерах: 12×12 (классический UI) и 14×14 для New UI
  (рисунок 12 px — как у стандартных):
  - платформа, по точным путям: Run, rerun, тест прошёл/упал, implementing / overriding / implemented / overridden / sibling,
    рекурсия, лямбда, закладка и мнемоника, цвет; брейкпоинты line / method / exception / disabled / muted и execution point;
  - Spring / JPA / Database (закрытые плагины Ultimate): подменяются в коде `IndustrialIconPatcher` по плагину, размеру исходной
    иконки и ключевым словам в имени файла (autowired, candidates, scheduled, listener, publisher, transactional,
    configuration properties, repository, mapping/endpoint, bean; entity, id, column, relationship; run SQL).
    Тем же способом подменяются узлы БД: data source, PK, FK, index, view, sequence, trigger, function, connect / disconnect, execute.
    Если какая-то иконка gutter этих плагинов не распозналась, в `idea.log` будет строка
    `Industrial Graphite: gutter icon without mapping: <плагин> <путь>` — по ней правило легко дописать в `PluginIconRules`.
- **Шрифт IBM Plex Mono** (SIL OFL 1.1, `src/main/resources/fonts/`): регистрируется при запуске и один раз ставится
  шрифтом интерфейса и редактора (размер остаётся твой). Схема редактора тоже указывает IBM Plex Mono.
  Если потом сменишь шрифт в Settings — плагин его не перетрёт.

## Установка
Settings → Plugins → ⚙ → **Install Plugin from Disk…** → `industrial-graphite-theme-1.0.0.zip` → перезапуск.
Settings → Appearance → Theme → **Industrial Graphite**.

## Сборка
```bash
./gradlew buildPlugin   # build/distributions/*.zip
./gradlew runIde        # песочница с плагином
```
JDK 17+, IntelliJ IDEA 2023.3+.

## Иконки
Исходные SVG лежат в `src/main/resources/icons/industrial/`. Файлы `@2x` не нужны — SVG масштабируется сам. Если добавляешь новую иконку, ужми её до стандартного размера: `python3 tools/fit_icons.py путь/к/иконке.svg`.
Иконки с бейджем бина (`*_bean.svg`) собираются скриптом:
```bash
python3 tools/compose_badges.py src/main/resources/icons/industrial
```
Схема редактора генерируется `tools/gen_scheme.py`.
