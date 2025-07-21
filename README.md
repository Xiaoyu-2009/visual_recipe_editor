# 可视化配方编写 (Workstation Recipe Exporter)

## 生成格式
- **JSON格式** - Minecraft原版的数据包配方格式
- **KubeJS格式** - KubeJS模组的JS配方格式

## 使用示例
```
/re <workstation_type>
/recipeexporter <workstation_type>
/recipeexporter <+下方对应的工作方块id打开不同界面> [示例：/recipeexporter furnace]
- crafting - 工作台
- furnace - 熔炉
- blast_furnace - 高炉
- smoker - 烟熏炉
- campfire - 营火[还未添加]
- stonecutter - 切石机
- smithing - 锻造台
```

## 生成位置

### JSON配方文件
- 路径：`<游戏目录>/exported_recipes/`
- 文件名格式：`<配方类型>_<时间戳>.json`

### KubeJS脚本文件
- 路径：`<游戏目录>/kubejs/server_scripts/`
- 文件名：`exported_recipes.js`

# Visual Recipe Writer (Workstation Recipe Exporter)

## Output Formats
- **JSON Format** - Minecraft's original data pack recipe format
- **KubeJS Format** - KubeJS mod's JavaScript recipe format

## Usage Examples/re <workstation_type>
/recipeexporter <workstation_type>
/recipeexporter <use the corresponding block ID below to open different interfaces> [Example: /recipeexporter furnace]
- crafting - Crafting Table
- furnace - Furnace
- blast_furnace - Blast Furnace
- smoker - Smoker
- campfire - Campfire [not yet added]
- stonecutter - Stonecutter
- smithing - Smithing Table
## Output Locations

### JSON Recipe Files
- Path: `<game directory>/exported_recipes/`
- Filename format: `<recipe_type>_<timestamp>.json`

### KubeJS Script Files
- Path: `<game directory>/kubejs/server_scripts/`
- Filename: `exported_recipes.js`
