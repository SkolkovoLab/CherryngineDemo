# CherryngineDemo

### [Russian README](https://github.com/SkolkovoLab/CherryngineDemo/blob/master/README.md)

A demo project built on [Cherryngine](https://github.com/SkolkovoLab/Cherryngine). Shows how a game mode is built on top of the engine — from hooking up platforms to gameplay systems.

## What it demonstrates

- An instance with the `gm_construct` map and an apartment system (`apart1`, `apart2`)
- Players rendered as axolotls with physical hitboxes (Jolt Physics)
- Dynamic objects — cubes with full-fledged physics
- Simultaneous support for Minecraft Java Edition and Bedrock Edition
- Layer system: each player has their own apartment at the same coordinates

## Structure

```
impl-demo/          — platform-agnostic part: ECS systems, components, renderers
impl-demo/minecraft — Minecraft Java implementation: renderers, tickables, platform module
impl-demo/bedrock   — Bedrock implementation
```

## Running

```bash
./gradlew :impl-demo:run
```

Server configuration: `impl-demo/run/application.yml`
