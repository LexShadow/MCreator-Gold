<#-- @formatter:off -->
<#include "../mcitems.ftl">
<#import "multi_noise.json.ftl" as ms>
<#import "surface_builder.json.ftl" as sb>
{
  "type": "${modid}:${registryname}",
  "generator": {
    "type": "minecraft:noise",
    "seed": 0,
    "biome_source": <@ms.multiNoiseSource/>,
    "settings": {
      "name": "${modid}:${registryname}",
      "sea_level": 63,
      "legacy_random_source": false,
      "disable_mob_generation": ${!data.imitateOverworldBehaviour},
      "noodle_caves_enabled": ${data.imitateOverworldBehaviour},
      "noise_caves_enabled": ${data.imitateOverworldBehaviour},
      "aquifers_enabled": ${data.imitateOverworldBehaviour},
      "deepslate_enabled": ${data.imitateOverworldBehaviour},
      "ore_veins_enabled": ${data.imitateOverworldBehaviour},
      "default_block": ${mappedMCItemToBlockStateJSON(data.mainFillerBlock)},
      "default_fluid": ${mappedMCItemToBlockStateJSON(data.fluidBlock)},
      "noise": {
        "min_y": 0,
        "height": 256,
        "size_horizontal": 1,
        "size_vertical": 2,
        "sampling": {
          "xz_scale": 1,
          "y_scale": 1,
          "xz_factor": 80,
          "y_factor": 160
        },
        "bottom_slide": {
          "target": 0.1171875,
          "size": 3,
          "offset": 0
        },
        "top_slide": {
          "target": -0.078125,
          "size": 2,
          "offset": 8
        },
        <#include "overworld_terrain_shaper.json.ftl">
      },
      <#include "overworld_noise_router.json.ftl">,
      "surface_rule": {
         "type": "minecraft:sequence",
         "sequence": [
           {
             "type": "minecraft:condition",
             "if_true": {
               "type": "minecraft:vertical_gradient",
               "random_name": "minecraft:bedrock_floor",
               "true_at_and_below": {
                 "above_bottom": 0
               },
               "false_at_and_above": {
                 "above_bottom": 5
               }
             },
             "then_run": {
               "type": "minecraft:block",
               "result_state": {
                 "Name": "minecraft:bedrock"
               }
             }
           },
           <#list data.biomesInDimension as biome>
             <#if biome.getUnmappedValue().startsWith("CUSTOM:")>
               <#assign ge = w.getWorkspace().getModElementByName(biome.getUnmappedValue().replace("CUSTOM:", "")).getGeneratableElement()/>
               <@sb.default biome ge.groundBlock ge.undergroundBlock/>
             <#else>
               <@sb.vanilla biome/>
             </#if>
             <#if biome?has_next>,</#if>
           </#list>
         ]
      },
      "structures": {
        "structures": {
          "minecraft:bastion_remnant": {
            "spacing": 27,
            "separation": 4,
            "salt": 30084232
          },
          "minecraft:buried_treasure": {
            "spacing": 1,
            "separation": 0,
            "salt": 0
          },
          "minecraft:desert_pyramid": {
            "spacing": 32,
            "separation": 8,
            "salt": 14357617
          },
          "minecraft:endcity": {
            "spacing": 20,
            "separation": 11,
            "salt": 10387313
          },
          "minecraft:fortress": {
            "spacing": 27,
            "separation": 4,
            "salt": 30084232
          },
          "minecraft:igloo": {
            "spacing": 32,
            "separation": 8,
            "salt": 14357618
          },
          "minecraft:jungle_pyramid": {
            "spacing": 32,
            "separation": 8,
            "salt": 14357619
          },
          "minecraft:mansion": {
            "spacing": 80,
            "separation": 20,
            "salt": 10387319
          },
          "minecraft:mineshaft": {
            "spacing": 1,
            "separation": 0,
            "salt": 0
          },
          "minecraft:monument": {
            "spacing": 32,
            "separation": 5,
            "salt": 10387313
          },
          "minecraft:nether_fossil": {
            "spacing": 2,
            "separation": 1,
            "salt": 14357921
          },
          "minecraft:ocean_ruin": {
            "spacing": 20,
            "separation": 8,
            "salt": 14357621
          },
          "minecraft:pillager_outpost": {
            "spacing": 32,
            "separation": 8,
            "salt": 165745296
          },
          "minecraft:ruined_portal": {
            "spacing": 40,
            "separation": 15,
            "salt": 34222645
          },
          "minecraft:shipwreck": {
            "spacing": 24,
            "separation": 4,
            "salt": 165745295
          },
          "minecraft:stronghold": {
            "spacing": 1,
            "separation": 0,
            "salt": 0
          },
          "minecraft:swamp_hut": {
            "spacing": 32,
            "separation": 8,
            "salt": 14357620
          },
          "minecraft:village": {
            "spacing": 34,
            "separation": 8,
            "salt": 10387312
          }
        }
      }
    }
  }
}
<#-- @formatter:on -->