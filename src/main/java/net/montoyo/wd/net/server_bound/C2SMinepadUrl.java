package net.montoyo.wd.net.server_bound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.montoyo.wd.item.ItemMinePad2;
import net.montoyo.wd.net.Packet;

import java.util.UUID;

public class C2SMinepadUrl extends Packet {
	UUID id;
	String url;
	
	public C2SMinepadUrl(UUID id, String url) {
		this.id = id;
		this.url = url;
	}
	
	public C2SMinepadUrl(FriendlyByteBuf buf) {
		super(buf);
		this.id = buf.readUUID();
		this.url = buf.readUtf();
	}
	
	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeUUID(id);
		buf.writeUtf(url);
	}
	
	@Override
	public void handle(NetworkEvent.Context ctx) {
		for (InteractionHand value : InteractionHand.values()) {
			ItemStack stack = ctx.getSender().getItemInHand(value);
			if (stack.getItem() instanceof ItemMinePad2 pad) {
				stack.getOrCreateTag().putUUID("PadID", id);
				stack.getOrCreateTag().putString("PadURL", url);
			}
		}
	}
}
