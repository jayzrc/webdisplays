/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.client.renderers;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.montoyo.wd.utilities.BlockSide;
import net.montoyo.wd.utilities.Vector3f;
import net.montoyo.wd.utilities.Vector3i;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ScreenBaker implements BakedModel {
	
	private static final List<BakedQuad> noQuads = ImmutableList.of();
	private final TextureAtlasSprite[] texs = new TextureAtlasSprite[16];
	private final BlockSide[] blockSides = BlockSide.values();
	private final Direction[] blockFacings = Direction.values();
	private final ModelState modelState;
	private final Function<net.minecraft.client.resources.model.Material, TextureAtlasSprite> spriteGetter;
	private final ItemOverrides overrides;
	private final ItemTransforms itemTransforms;
	
	IntegerModelProperty[] TEXTURES = new IntegerModelProperty[6];
	
	public ScreenBaker(ModelState modelState, Function<net.minecraft.client.resources.model.Material, TextureAtlasSprite> spriteGetter, ItemOverrides overrides, ItemTransforms itemTransforms) {
		this.modelState = modelState;
		this.spriteGetter = spriteGetter;
		this.overrides = overrides;
		this.itemTransforms = itemTransforms;
		
		for (int i = 0; i < texs.length; i++) {
			texs[i] = spriteGetter.apply(ScreenModelLoader.MATERIALS_SIDES[i]);
		}
		
		for (int i = 0; i < TEXTURES.length; i++) {
			TEXTURES[i] = new IntegerModelProperty();
		}
	}
	
	private void putVertex(int[] buf, int pos, Vector3f vpos, TextureAtlasSprite tex, Vector3f uv, Vector3i normal) {
		buf[pos * 8 + 0] = Float.floatToRawIntBits(vpos.x);
		buf[pos * 8 + 1] = Float.floatToRawIntBits(vpos.y);
		buf[pos * 8 + 2] = Float.floatToRawIntBits(vpos.z);
		buf[pos * 8 + 3] = 0xFFFFFFFF; //Color, let this white...
		buf[pos * 8 + 4] = Float.floatToRawIntBits(tex.getU(uv.x));
		buf[pos * 8 + 5] = Float.floatToRawIntBits(tex.getV(uv.y));
		
		int nx = (normal.x * 127) & 0xFF;
		int ny = (normal.y * 127) & 0xFF;
		int nz = (normal.z * 127) & 0xFF;
		buf[pos * 8 + 7] = nx | (ny << 8) | (nz << 16);
	}
	
	private Vector3f rotateVec(Vector3f vec, BlockSide side) {
		return switch (side) {
			case BOTTOM -> new Vector3f(vec.x, 1.0f, 1.0f - vec.z);
			case TOP -> new Vector3f(vec.x, 0.0f, vec.z);
			case NORTH -> new Vector3f(vec.x, vec.z, 1.0f);
			case SOUTH -> new Vector3f(vec.x, 1.0f - vec.z, 0.0f);
			case WEST -> new Vector3f(1.f, vec.x, vec.z);
			case EAST -> new Vector3f(0.0f, 1.0f - vec.x, vec.z);
			//noinspection UnnecessaryDefault
			default -> throw new RuntimeException("Unknown block side " + side);
		};
	}
	
	private Vector3f rotateTex(BlockSide side, float u, float v) {
		return switch (side) {
			case BOTTOM -> new Vector3f(16.f - u, 16.f - v, 0.0f);
			case TOP -> new Vector3f(16.f - u, v, 0.0f);
			case NORTH -> new Vector3f(16.f - u, 16.f - v, 0.0f);
			case SOUTH -> new Vector3f(u, v, 0.0f);
			case WEST -> new Vector3f(16.f - v, u, 0.0f);
			case EAST -> new Vector3f(v, 16.f - u, 0.0f);
			//noinspection UnnecessaryDefault
			default -> throw new RuntimeException("Unknown block side " + side);
		};
	}
	
	private BakedQuad bakeSide(BlockSide side, TextureAtlasSprite tex) {
		int[] data = new int[8 * 4];
		
		int rotation = switch (side) {
			case NORTH -> 0;
			case SOUTH -> 0;
			case EAST -> 0;
			case WEST -> 0;
			case TOP -> 0;
			case BOTTOM -> 0;
			//noinspection UnnecessaryDefault
			default -> throw new RuntimeException("Unknown block side " + side);
		};
		
		putVertex(data, (rotation + 3) % 4, rotateVec(new Vector3f(0.0f, 0.0f, 0.0f), side), tex, rotateTex(side, 16.0f, 0.0f), side.backward);
		putVertex(data, (rotation + 2) % 4, rotateVec(new Vector3f(0.0f, 0.0f, 1.0f), side), tex, rotateTex(side, 16.0f, 16.0f), side.backward);
		putVertex(data, (rotation + 1) % 4, rotateVec(new Vector3f(1.0f, 0.0f, 1.0f), side), tex, rotateTex(side, 0.0f, 16.0f), side.backward);
		putVertex(data, (rotation + 0) % 4, rotateVec(new Vector3f(1.0f, 0.0f, 0.0f), side), tex, rotateTex(side, 0.0f, 0.0f), side.backward);
		
		return new BakedQuad(data, 0xFFFFFFFF, blockFacings[side.ordinal()].getOpposite(), tex, true);
	}
	
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random) {
		return getQuads(state, side, random, ModelData.EMPTY, null);
	}
	
	@Override
	public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
		if (side == null)
			return noQuads;
		
		List<BakedQuad> ret = new ArrayList<>();
		
		int sid = BlockSide.reverse(side.ordinal());
		BlockSide s = blockSides[sid];
		TextureAtlasSprite tex = texs[15];
		if (data.has(TEXTURES[side.ordinal()]))
				tex = texs[data.get(TEXTURES[side.ordinal()])];
		ret.add(bakeSide(s, tex));
		return ret;
	}
	
	@Override
	public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
		ModelData.Builder builder = ModelData.builder();
		for (int i = 0; i < TEXTURES.length; i++) {
			int res = 0;
			BlockSide side = blockSides[i];
			BlockState u = level.getBlockState(pos.offset(side.up.x, side.up.y, side.up.z));
			BlockState d = level.getBlockState(pos.offset(-side.up.x, -side.up.y, -side.up.z));
			if (
					u.getBlock() == state.getBlock() &&
							d.getBlock() != state.getBlock()
			) res = 1;
			else res = 4;
			
			BlockState r = level.getBlockState(pos.offset(side.right.x, side.right.y, side.right.z));
			BlockState l = level.getBlockState(pos.offset(-side.right.x, -side.right.y, -side.right.z));
			if (
					r.getBlock() == state.getBlock() &&
							l.getBlock() != state.getBlock()
			) {
				if (res == 1) res = 9;
				else if (res == 4) res = 12;
				else res = 8;
			}
			
			builder.with(TEXTURES[i], res);
		}
		return builder.build();
	}
	
	@Override
	public boolean useAmbientOcclusion() {
		return true;
	}
	
	@Override
	public boolean isGui3d() {
		return true;
	}
	
	@Override
	public boolean usesBlockLight() {
		return false;
	}
	
	@Override
	public boolean isCustomRenderer() {
		return false;
	}
	
	@Override
	@Nonnull
	public TextureAtlasSprite getParticleIcon() {
		return texs[15];
	}
	
	@Override
	@Nonnull
	public ItemTransforms getTransforms() {
		return ItemTransforms.NO_TRANSFORMS;
	}
	
	@Override
	@Nonnull
	public ItemOverrides getOverrides() {
		return ItemOverrides.EMPTY;
	}
	
	//@formatter:off
    public final class IntegerModelProperty extends ModelProperty<Integer> {}
    //@formatter:on
}
