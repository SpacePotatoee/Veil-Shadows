# Veil Shadows

Just adds simple shadow mapping using veil

The shadows don't follow the sun lol

If you want to edit the angle of the shadows, go to the [createShadowModelView()](https://github.com/SpacePotatoee/Veil-Shadows/blob/master/src/main/java/com/sp/VeilShadowsClient.java#L70) method.

If you want to edit the strength/smoothness of the shadows, change the respective definitions in the [Solid](https://github.com/SpacePotatoee/Veil-Shadows/blob/master/src/main/resources/assets/veilshadows/pinwheel/shaders/program/minecraft_core/rendertype_solid.fsh#L7) and [Translucent](https://github.com/SpacePotatoee/Veil-Shadows/blob/master/src/main/resources/assets/veilshadows/pinwheel/shaders/program/minecraft_core/rendertype_translucent.fsh#L7) shaders.
