package gg.itzkatze.katze.forcebattle.gui;

import gg.itzkatze.katze.gui.KatzeGUI;
import gg.itzkatze.katze.gui.GUIClickableItem;
import gg.itzkatze.katze.gui.GUIItem;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.ItemStackCreator;
import gg.itzkatze.katze.util.KatzeColor;
import gg.itzkatze.katze.util.Skull;
import gg.itzkatze.katze.util.StringUtility;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

public class CraftingRecipeGUI extends KatzeGUI {

    private final ItemStack result;
    private final List<Recipe> recipes;
    private final int page;
    private static final int[] GRID_SLOTS = {1, 2, 3, 10, 11, 12, 19, 20, 21};
    private static final int TRANSFORM_SLOT = 5;

    public CraftingRecipeGUI(ItemStack result) {
        this(result, 0);
    }

    private CraftingRecipeGUI(ItemStack result, int page) {
        super("Recipe for " + StringUtility.formatName(result.getType().name()) + " | Page: " + (page + 1), 27);
        this.result = result;
        this.recipes = Bukkit.getRecipesFor(result);
        this.page = page;
    }

    @Override
    public void setItems(KatzePlayer player) {
        fill(ItemStackCreator.fillerItem());

        set(new GUIItem(16) {
            @Override
            public ItemStack getItem(KatzePlayer player) {
                return result;
            }
        });

        set(new GUIItem(14) {
            @Override
            public ItemStack getItem(KatzePlayer player) {
                return ItemStackCreator.getSkull(Skull.ARROW_RIGHT, Component.text(" "), 1);
            }
        });

        for (int slot : GRID_SLOTS) {
            set(new GUIItem(slot) {
                @Override
                public ItemStack getItem(KatzePlayer player) {
                    return ItemStackCreator.getStack(Component.text(" "), Material.AIR, 1);
                }
            });
        }

        if (recipes.size() > 1) {
            if (page > 0) set(GUIClickableItem.getGoBackItem(0, new CraftingRecipeGUI(result, page - 1)));
            if (page < recipes.size() - 1) set(GUIClickableItem.getGoForthItem(8, new CraftingRecipeGUI(result, page + 1)));
        }

        if (recipes.isEmpty()) {
            set(new GUIItem(11) {
                @Override
                public ItemStack getItem(KatzePlayer player) {
                    ItemStack barrier = new ItemStack(Material.BARRIER);
                    ItemMeta meta = barrier.getItemMeta();
                    if (meta != null) meta.displayName(Component.text("No recipe found").color(KatzeColor.RED));
                    barrier.setItemMeta(meta);
                    return barrier;
                }
            });
            return;
        }

        Recipe recipe = recipes.get(page);
        ItemStack[] grid = new ItemStack[9];

        if (recipe instanceof ShapedRecipe shaped) {
            Map<Character, RecipeChoice> choiceMap = shaped.getChoiceMap();
            String[] shape = shaped.getShape();
            for (int row = 0; row < Math.min(shape.length, 3); row++) {
                String line = shape[row];
                for (int col = 0; col < Math.min(line.length(), 3); col++) {
                    char c = line.charAt(col);
                    RecipeChoice choice = choiceMap.get(c);
                    grid[row * 3 + col] = getChoiceItem(choice);
                }
            }

        } else if (recipe instanceof ShapelessRecipe shapeless) {
            List<RecipeChoice> ingredients = shapeless.getChoiceList();
            for (int i = 0; i < Math.min(ingredients.size(), 9); i++) {
                grid[i] = getChoiceItem(ingredients.get(i));
            }

        } else if (recipe instanceof CookingRecipe<?> cooking) {
            ItemStack input = getChoiceItem(cooking.getInputChoice());
            Material icon = switch (recipe) {
                case BlastingRecipe ignored -> Material.BLAST_FURNACE;
                case SmokingRecipe ignored -> Material.SMOKER;
                case CampfireRecipe ignored -> Material.CAMPFIRE;
                default -> Material.FURNACE;
            };
            setSimpleProcess(input, icon, Component.text(StringUtility.formatName(icon.name())).color(KatzeColor.LIGHT_GRAY));

        } else if (recipe instanceof StonecuttingRecipe stonecutting) {
            ItemStack input = getChoiceItem(stonecutting.getInputChoice());
            setSimpleProcess(input, Material.STONECUTTER, Component.text("Stonecutting").color(KatzeColor.LIGHT_GRAY));

        } else if (recipe instanceof SmithingTransformRecipe smithing) {
            try {
                RecipeChoice base = smithing.getBase();
                RecipeChoice addition = smithing.getAddition();
                RecipeChoice template = smithing.getTemplate();

                ItemStack baseItem = getChoiceItem(base);
                ItemStack additionItem = getChoiceItem(addition);
                ItemStack templateItem = getChoiceItem(template);

                setItemSafe(2, templateItem);
                setItemSafe(10, baseItem);
                setItemSafe(12, additionItem);

                set(new GUIItem(TRANSFORM_SLOT) {
                    @Override
                    public ItemStack getItem(KatzePlayer player) {
                        ItemStack table = new ItemStack(Material.SMITHING_TABLE);
                        ItemMeta meta = table.getItemMeta();
                        if (meta != null) meta.displayName(Component.text("Smithing").color(KatzeColor.LIGHT_GRAY));
                        table.setItemMeta(meta);
                        return table;
                    }
                });
            } catch (Exception ignored) {}

        }

        centerAndPlaceGrid(grid);
    }

    private void setSimpleProcess(ItemStack input, Material icon, Component name) {
        final ItemStack inputFinal = input;
        set(new GUIItem(11) {
            @Override
            public ItemStack getItem(KatzePlayer player) {
                return inputFinal != null ? inputFinal : ItemStackCreator.getStack(Component.text(" "), Material.AIR, 1);
            }
        });
        set(new GUIItem(TRANSFORM_SLOT) {
            @Override
            public ItemStack getItem(KatzePlayer player) {
                ItemStack item = new ItemStack(icon);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) meta.displayName(name);
                item.setItemMeta(meta);
                return item;
            }
        });
    }

    private void setItemSafe(int slot, ItemStack stack) {
        if (stack == null) return;
        final ItemStack s = stack;
        set(new GUIItem(GRID_SLOTS[Math.min(slot, GRID_SLOTS.length - 1)]) {
            @Override
            public ItemStack getItem(KatzePlayer player) {
                return s;
            }
        });
    }

    private ItemStack getChoiceItem(RecipeChoice choice) {
        return switch (choice) {
            case RecipeChoice.MaterialChoice mat when !mat.getChoices().isEmpty() -> new ItemStack(mat.getChoices().getFirst());
            case RecipeChoice.ExactChoice exact when !exact.getChoices().isEmpty() -> exact.getChoices().getFirst().clone();
            case null, default -> null;
        };
    }

    private void centerAndPlaceGrid(ItemStack[] grid) {
        int minRow = 3, maxRow = -1, minCol = 3, maxCol = -1;
        for (int i = 0; i < 9; i++) {
            if (grid[i] != null) {
                int row = i / 3, col = i % 3;
                minRow = Math.min(minRow, row);
                maxRow = Math.max(maxRow, row);
                minCol = Math.min(minCol, col);
                maxCol = Math.max(maxCol, col);
            }
        }

        int usedWidth = (maxCol - minCol + 1);
        int usedHeight = (maxRow - minRow + 1);
        int rowOffset = (usedHeight < 3) ? (1 - (minRow + usedHeight / 2)) : 0;
        int colOffset = (usedWidth < 3) ? (1 - (minCol + usedWidth / 2)) : 0;

        for (int i = 0; i < 9; i++) {
            ItemStack ingredient = grid[i];
            if (ingredient == null) continue;
            int row = i / 3 + rowOffset;
            int col = i % 3 + colOffset;
            if (row < 0 || row > 2 || col < 0 || col > 2) continue;

            int guiSlot = GRID_SLOTS[row * 3 + col];
            final ItemStack finalIngredient = ingredient;
            set(new GUIClickableItem(guiSlot) {
                @Override
                public void run(final InventoryClickEvent event, final KatzePlayer player) {
                    new CraftingRecipeGUI(finalIngredient).open(player);
                }

                @Override
                public ItemStack getItem(KatzePlayer player) {
                    return finalIngredient;
                }
            });
        }
    }

    @Override
    public boolean allowItemMoving() {
        return false;
    }
}
