# План: принять старый стеш целиком (engine submodule + impl-demo)

## Цель
Завершить применение стеша так, чтобы **оба** репозитория компилировались:
объединить рефактор стеша (`@TickablePriority`, переименования, удаление событий)
с новой архитектурой master (стадия `INPUT`, клиент-сторона вынесена в demo).

Стеш = 4 независимых рефактора. Большая часть применилась чисто (staged),
конфликты — точки столкновения с master.

---

## A. Конфликты (4 файла, engine)
1. **TickStage.kt** — взять `object` из стеша + добавить `INPUT = -2`,
   сохранив KDoc из HEAD. Итог: `INPUT=-2, PRE=-1, GAME=0, POST=1`.
   (merged `Instance.kt` сортирует по `intValue(...).orElse(TickStage.GAME)` →
   `TickStage.*` обязаны быть `Int`.)
2. **MinecraftPlayer.kt** — взять HEAD, выкинуть stashed-блок `teleport`.
   Обоснование: в HEAD у MinecraftPlayer нет `clientPosition`/`clientYawPitch`;
   `MinecraftPlayer.teleport` никто не зовёт (проверено: все `.teleport(` —
   на других объектах).
3. **ReadClientPositionSystem.kt** — принять удаление (HEAD). Не используется нигде.
4. **WriteClientPositionSystem.kt** — принять удаление (HEAD). Не используется нигде.

## B. Миграция аннотаций тиков (13 файлов: 1 engine + 12 demo)
`@InstanceSingleton(..., stage = TickStage.X)` →
`@InstanceSingleton(...)` + `@TickablePriority(stage = TickStage.X)` + импорт.
- engine: `MinecraftInputSnapshotTickable` (INPUT)
- demo: MinecraftCommandTickable, PlayerPositionPreSyncTickable,
  PlayerPositionPostSyncTickable, MinecraftHotbarSyncTickable,
  MinecraftThirdPersonCameraTickable, MinecraftPlayerPlatformTickable,
  BedrockCommandTickable, PlayerHitboxPostSyncTickable, PlayerHitboxPreSyncTickable,
  MinecraftToolUseTickable, BedrockHotbarSyncTickable, BedrockToolUseTickable

## C. Переименование полей компонентов (через ide_refactor_rename)
Стеш переименовал только поля компонентов; `Player.viewContextIDs` (интерфейс) — НЕ трогаем.
Чинит ~40 сломанных ссылок семантически (а не текстом):
1. `ide_refactor_rename` PlayerComponent: `presentInContextIds` → `viewContextIDs`
   (вернуть имя, чтобы ссылки резолвились).
2. `ide_refactor_rename` PlayerComponent: `viewContextIDs` → `presentInContextIds`
   (relatedRenamingStrategy=none) — тянет за собой все настоящие ссылки.
3-4. То же для ViewableComponent: `visibleInContextIds`→`viewContextIDs`→`visibleInContextIds`.

## D. Удаление старой системы событий
- Принять удаления стеша: `EcsEvent.kt`, `LastPlayerPositionEvent.kt`,
  `ClearEventsSystem.kt` (код-юзеров нет).
- demo: убрать `ClearEventsConfig` из `DemoInit.kt` + удалить объект и импорт в
  `CoreSystemConfigs.kt`.
- **@OneShotComponent — РЕШЕНИЕ ПРИНЯТО: убрать оба.**
  `git rm` для `OneShotComponent.kt` и `MicronautUtils.kt` (оба staged-new, ноль
  потребителей). Никакого мёртвого кода. Обработчик не пишем.

## E. Уже применено чисто из стеша (проверить, не трогать без нужды)
Instance.kt, InstanceSingleton.kt, TickablePriority.kt, build.gradle (Fleks 2.13),
EcsWorldTickable, MinecraftViewTickable, JavaToBedrockViewTickable.
- Возможный фикс: `@Retention(RUNTIME)` на `TickablePriority`, если Micronaut не
  прочитает `intValue` (проверю билдом).

## F. Финализация
- `git add -A` в engine; убедиться, что маркеров конфликта нет.
- Сборка через IDE/gradle; чинить ошибки (≤3 итерации).
- **НЕ коммитить** и **не делать `git stash drop`** без отдельной команды.

## Проверка (критерии готовности)
- `grep '<<<<<<<\|>>>>>>>'` = 0.
- `stage = TickStage` остаётся только внутри `@TickablePriority(...)` и в доках.
- Сборка engine + impl-demo зелёная.
- `viewContextIDs` переименован на компонентах, на `Player`/реализациях — сохранён.
