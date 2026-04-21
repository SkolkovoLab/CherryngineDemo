# CherryngineDemo

### [English README](https://github.com/SkolkovoLab/CherryngineDemo/blob/master/README_EN.md)

Демонстрационный проект на [Cherryngine](https://github.com/SkolkovoLab/Cherryngine). Показывает как строится игровой режим поверх движка — от подключения платформ до геймплейных систем.

## Что показывает

- Инстанс с картой `gm_construct` и системой квартир (`apart1`, `apart2`)
- Игроки в виде аксолотлей с физическими хитбоксами (Jolt Physics)
- Динамические объекты — кубы с полноценной физикой
- Одновременная поддержка Minecraft Java Edition и Bedrock Edition
- Система слоёв: у каждого игрока своя квартира на одних координатах

## Структура

```
impl-demo/          — платформонезависимая часть: ECS системы, компоненты, рендереры
impl-demo/minecraft — Minecraft Java реализация: рендереры, тикаблы, платформенный модуль
impl-demo/bedrock   — Bedrock реализация
```

## Запуск

```bash
./gradlew :impl-demo:run
```

Конфигурация сервера: `impl-demo/run/application.yml`

