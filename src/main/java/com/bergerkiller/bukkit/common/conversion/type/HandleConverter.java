package com.bergerkiller.bukkit.common.conversion.type;

import net.minecraft.server.v1_4_R1.Block;
import net.minecraft.server.v1_4_R1.ChunkCoordinates;
import net.minecraft.server.v1_4_R1.ChunkPosition;
import net.minecraft.server.v1_4_R1.Entity;
import net.minecraft.server.v1_4_R1.Item;
import net.minecraft.server.v1_4_R1.Vec3D;
import net.minecraft.server.v1_4_R1.World;

import org.bukkit.GameMode;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_4_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_4_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_4_R1.block.*;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_4_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_4_R1.inventory.CraftItemStack;
import org.bukkit.util.Vector;

import com.bergerkiller.bukkit.common.conversion.BasicConverter;
import com.bergerkiller.bukkit.common.conversion.Conversion;
import com.bergerkiller.bukkit.common.nbt.CommonTag;
import com.bergerkiller.bukkit.common.protocol.CommonPacket;
import com.bergerkiller.bukkit.common.reflection.classes.BlockStateRef;
import com.bergerkiller.bukkit.common.reflection.classes.CraftItemStackRef;
import com.bergerkiller.bukkit.common.reflection.classes.EnumGamemodeRef;
import com.bergerkiller.bukkit.common.reflection.classes.WorldTypeRef;
import com.bergerkiller.bukkit.common.utils.CommonUtil;
import com.bergerkiller.bukkit.common.utils.LogicUtil;
import com.bergerkiller.bukkit.common.utils.NBTUtil;
import com.bergerkiller.bukkit.common.wrappers.PlayerAbilities;

/**
 * Converter for converting to internal handles (from wrapper classes)<br>
 * <b>Do not reference external state-classes while constructing (e.g. reflection classes)</b>
 */
public abstract class HandleConverter extends BasicConverter<Object> {
	public static final HandleConverter toEntityHandle = new HandleConverter("Entity") {
		@Override
		public Object convertSpecial(Object value, Class<?> valueType, Object def) {
			if (value instanceof CraftEntity) {
				return ((CraftEntity) value).getHandle();
			} else {
				return def;
			}
		}

		@Override
		public boolean isCastingSupported() {
			return true;
		}
	};
	public static final HandleConverter toWorldHandle = new HandleConverter("World") {
		@Override
		public Object convertSpecial(Object value, Class<?> valueType, Object def) {
			org.bukkit.World world = WrapperConverter.toWorld.convert(value);
			if (world instanceof CraftWorld) {
				return ((CraftWorld) world).getHandle();
			} else {
				return def;
			}
		}

		@Override
		public boolean isCastingSupported() {
			return true;
		}
	};
	public static final HandleConverter toChunkHandle = new HandleConverter("Chunk") {
		@Override
		public Object convertSpecial(Object value, Class<?> valueType, Object def) {
			org.bukkit.Chunk chunk = WrapperConverter.toChunk.convert(value);
			if (chunk instanceof CraftChunk) {
				return ((CraftChunk) value).getHandle();
			} else {
				return def;
			}
		}
	};
	public static final HandleConverter toItemStackHandle = new HandleConverter("ItemStack") {
		@Override
		public Object convertSpecial(Object value, Class<?> valueType, Object def) {
			if (value instanceof CraftItemStack) {
				return CraftItemStackRef.handle.get(value);
			} else if (value instanceof org.bukkit.inventory.ItemStack) {
				return CraftItemStack.asNMSCopy((org.bukkit.inventory.ItemStack) value);
			} else {
				return def;
			}
		}
	};
	public static final HandleConverter toTileEntityHandle = new HandleConverter("TileEntity") {
		@Override
		public Object convertSpecial(Object value, Class<?> valueType, Object def) {
			if (value instanceof CraftSign) {
				return BlockStateRef.SIGN.get(value);
			} else if (value instanceof CraftFurnace) {
				return BlockStateRef.FURNACE.get(value);
			} else if (value instanceof CraftChest) {
				return BlockStateRef.CHEST.get(value);
			} else if (value instanceof CraftDispenser) {
				return BlockStateRef.DISPENSER.get(value);
			} else if (value instanceof BlockState) {
				value = ((BlockState) value).getBlock();
			}
			if (value instanceof org.bukkit.block.Block) {
				// Obtain the tile entity at this block if possible
				org.bukkit.block.Block b = (org.bukkit.block.Block) value;
				World world = (World) toWorldHandle.convert(b.getWorld());
				if (world != null) {
					return LogicUtil.fixNull(world.getTileEntity(b.getX(), b.getY(), b.getZ()), def);
				}
			}
			return def;
		}

		@Override
		public boolean isCastingSupported() {
			return true;
		}
	};
	public static final HandleConverter toInventoryHandle = new HandleConverter("IInventory") {
		@Override
		public Object convertSpecial(Object value, Class<?> valueType, Object def) {
			if (value instanceof CraftInventory) {
				return LogicUtil.fixNull(((CraftInventory) value).getInventory(), def);
			}
			return def;
		}
	};
	public static final HandleConverter toDataWatcherHandle = new HandleConverter("DataWatcher") {
		@Override
		public Object convertSpecial(Object value, Class<?> valueType, Object def) {
			if (value instanceof com.bergerkiller.bukkit.common.wrappers.DataWatcher) {
				return ((com.bergerkiller.bukkit.common.wrappers.DataWatcher) value).getHandle();
			} else if (value instanceof Entity) {
				return ((Entity) value).getDataWatcher();
			} else {
				return def;
			}
		}
	};
	public static final HandleConverter toNBTTagHandle = new HandleConverter("NBTBase") {
		@Override
		public Object convertSpecial(Object value, Class<?> valueType, Object def) {
			if (value instanceof CommonTag) {
				return ((CommonTag) value).getHandle();
			} else {
				try {
					return NBTUtil.createHandle(null, value);
				} catch (Exception ex) {
					return def;
				}
			}
		}

		@Override
		public boolean isCastingSupported() {
			return true;
		}
	};
	public static final HandleConverter toItemHandle = new HandleConverter("Item") {
		@Override
		public Object convertSpecial(Object value, Class<?> valueType, Object def) {
			Integer id = PropertyConverter.toItemId.convert(value);
			if (id != null) {
				int idInt = id.intValue();
				if (LogicUtil.isInBounds(Item.byId, idInt)) {
					return LogicUtil.fixNull(Item.byId[idInt], def);
				}
			}
			return def;
		}

		@Override
		public boolean isCastingSupported() {
			return true;
		}
	};
	public static final HandleConverter toBlockHandle = new HandleConverter("Block") {
		@Override
		public Object convertSpecial(Object value, Class<?> valueType, Object def) {
			Integer id = PropertyConverter.toItemId.convert(value);
			if (id != null) {
				int idInt = id.intValue();
				if (LogicUtil.isInBounds(Block.byId, idInt)) {
					return LogicUtil.fixNull(Block.byId[idInt], def);
				}
			}
			return def;
		}

		@Override
		public boolean isCastingSupported() {
			return true;
		}
	};
	public static final HandleConverter toGameModeHandle = new HandleConverter("EnumGamemode") {
		@Override
		public Object convertSpecial(Object value, Class<?> valueType, Object def) {
			GameMode gameMode = Conversion.toGameMode.convert(value);
			if (gameMode != null) {
				return EnumGamemodeRef.getFromId.invoke(null, gameMode.getValue());
			}
			return def;
		}
	};
	public static final HandleConverter toWorldTypeHandle = new HandleConverter("WorldType") {
		@Override
		public Object convertSpecial(Object value, Class<?> valueType, Object def) {
			org.bukkit.WorldType type = Conversion.toWorldType.convert(value);
			if (type != null) {
				return WorldTypeRef.getType.invoke(null, type.getName());
			} else {
				return def;
			}
		}
	};
	public static final HandleConverter toPacketHandle = new HandleConverter("Packet") {
		@Override
		public Object convertSpecial(Object value, Class<?> valueType, Object def) {
			if (value instanceof CommonPacket) {
				return ((CommonPacket) value).getHandle();
			} else {
				return def;
			}
		}
	};
	public static final HandleConverter toChunkCoordIntPairHandle = new HandleConverter("ChunkCoordIntPair") {
		@Override
		public Object convertSpecial(Object value, Class<?> valueType, Object def) {
			return def;
		}
	};
	public static final HandleConverter toChunkCoordinatesHandle = new HandleConverter("ChunkCoordinates") {
		@Override
		public Object convertSpecial(Object value, Class<?> valueType, Object def) {
			if (value instanceof ChunkPosition) {
				ChunkPosition pos = (ChunkPosition) value;
				return new ChunkCoordinates(pos.x, pos.y, pos.z);
			} else {
				return def;
			}
		}
	};
	public static final HandleConverter toChunkPositionHandle = new HandleConverter("ChunkPosition") {
		@Override
		public Object convertSpecial(Object value, Class<?> valueType, Object def) {
			if (value instanceof ChunkCoordinates) {
				ChunkCoordinates coord = (ChunkCoordinates) value;
				return new ChunkPosition(coord.x, coord.y, coord.z);
			} else {
				return def;
			}
		}
	};
	public static final HandleConverter toVec3DHandle = new HandleConverter("Vec3D") {
		@Override
		public Object convertSpecial(Object value, Class<?> valueType, Object def) {
			Vector vec = WrapperConverter.toVector.convert(value);
			if (vec != null) {
				return Vec3D.a(vec.getX(), vec.getY(), vec.getZ());
			} else {
				return def;
			}
		}
	};
	public static final HandleConverter toPlayerAbilitiesHandle = new HandleConverter("PlayerAbilities") {
		@Override
		protected Object convertSpecial(Object value, Class<?> valueType, Object def) {
			if (value instanceof PlayerAbilities) {
				return ((PlayerAbilities) value).getHandle();
			} else {
				return def;
			}
		}
	};

	public HandleConverter(String outputTypeName) {
		this(CommonUtil.getNMSClass(outputTypeName));
	}

	@SuppressWarnings("unchecked")
	public HandleConverter(Class<?> outputType) {
		super((Class<Object>) outputType);
	}
}
