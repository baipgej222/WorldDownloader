package wdl.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import wdl.WDL;
import wdl.api.IWDLMod;
import wdl.api.WDLApi;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.ResourceLocation;

public class GuiWDLExtensions extends GuiScreen {
	/**
	 * Top of the bottom list.
	 */
	private int bottomLocation;
	
	/**
	 * Height of the bottom area.
	 */
	private static final int TOP_HEIGHT = 23;
	/**
	 * Height of the middle section.
	 * 
	 * Equal to <code>{@link FontRenderer#FONT_HEIGHT} + 10</code>.
	 */
	private static final int MIDDLE_HEIGHT = 19;
	/**
	 * Height of the top area.
	 */
	private static final int BOTTOM_HEIGHT = 32;
	
	private class ModList extends GuiListExtended {
		public ModList() {
			super(GuiWDLExtensions.this.mc, GuiWDLExtensions.this.width,
					bottomLocation, TOP_HEIGHT, bottomLocation, 20);
			this.showSelectionBox = true;
		}
		
		private class ModEntry implements IGuiListEntry {
			public final IWDLMod mod;
			private final String modDesc;
			
			public ModEntry(IWDLMod mod) {
				this.mod = mod;
				this.modDesc = mod.getName() + " v" + mod.getVersion();
			}
			
			@Override
			public void drawEntry(int slotIndex, int x, int y, int listWidth,
					int slotHeight, int mouseX, int mouseY, boolean isSelected) {
				int centerY = y + slotHeight / 2
						- fontRendererObj.FONT_HEIGHT / 2;
				fontRendererObj.drawString(modDesc, x, centerY, 0xFFFFFF);
			}

			@Override
			public boolean mousePressed(int slotIndex, int x, int y,
					int mouseEvent, int relativeX, int relativeY) {
				if (selectedElement != slotIndex) {
					selectedElement = slotIndex;
					
					mc.getSoundHandler().playSound(
							PositionedSoundRecord.createPositionedSoundRecord(
									new ResourceLocation("gui.button.press"),
									1.0F));
					return true;
				}
				
				return false;
			}

			@Override
			public void mouseReleased(int slotIndex, int x, int y,
					int mouseEvent, int relativeX, int relativeY) {
			}

			@Override
			public void setSelected(int slotIndex, int p_178011_2_,
					int p_178011_3_) {
				
			}
		}
		
		private List<IGuiListEntry> entries = new ArrayList<IGuiListEntry>() {{
			for (IWDLMod mod : WDLApi.getWDLMods().values()) {
				add (new ModEntry(mod));
			}
		}};
		
		@Override
		public void drawScreen(int mouseX, int mouseY, float partialTicks) {
			this.height = this.bottom = bottomLocation;
			
			super.drawScreen(mouseX, mouseY, partialTicks);
		}
		
		@Override
		public IGuiListEntry getListEntry(int index) {
			return entries.get(index);
		}

		@Override
		protected int getSize() {
			return entries.size();
		}
		
		@Override
		protected boolean isSelected(int slotIndex) {
			return slotIndex == selectedElement;
		}
		
		@Override
		public int getListWidth() {
			return GuiWDLExtensions.this.width - 20;
		}
		
		@Override
		protected int getScrollBarX() {
			return GuiWDLExtensions.this.width - 10;
		}
		
		@Override
		public void func_178039_p() {
			if (mouseY < bottomLocation) {
				super.func_178039_p();
			}
		}
	}
	
	private class ModDetailList extends GuiListExtended {
		public ModDetailList() {
			super(GuiWDLExtensions.this.mc, GuiWDLExtensions.this.width,
					GuiWDLExtensions.this.height - bottomLocation,
					MIDDLE_HEIGHT, GuiWDLExtensions.this.height - bottomLocation
							- BOTTOM_HEIGHT, fontRendererObj.FONT_HEIGHT + 1);
		}
		
		@Override
		public void drawScreen(int mouseX, int mouseY, float partialTicks) {
			GlStateManager.translate(0, bottomLocation, 0);
			
			this.height = GuiWDLExtensions.this.height - bottomLocation;
			this.bottom = this.height - 32;
			
			super.drawScreen(mouseX, mouseY, partialTicks);
			
			drawCenteredString(fontRendererObj, "Details (resizable)",
					GuiWDLExtensions.this.width / 2, 5, 0xFFFFFF);
			
			GlStateManager.translate(0, -bottomLocation, 0);
		}
		
		@Override
		public IGuiListEntry getListEntry(int index) {
			return null;
		}
		
		@Override
		protected int getSize() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		/**
		 * Used by the drawing routine; edited to reduce weirdness.
		 * 
		 * (Don't move the bottom with the size of the screen).
		 */
		@Override
		protected void overlayBackground(int y1, int y2,
				int alpha1, int alpha2) {
			if (y1 == 0) {
				super.overlayBackground(y1, y2, alpha1, alpha2);
				return;
			} else {
				GlStateManager.translate(0, -bottomLocation, 0);
				
				super.overlayBackground(y1 + bottomLocation, y2
						+ bottomLocation, alpha1, alpha2);
				
				GlStateManager.translate(0, bottomLocation, 0);
			}
		}
		
		@Override
		public int getListWidth() {
			return GuiWDLExtensions.this.width - 20;
		}
		
		@Override
		protected int getScrollBarX() {
			return GuiWDLExtensions.this.width - 10;
		}
		
		@Override
		public void func_178039_p() {
			mouseY -= bottomLocation;
			
			if (mouseY > 0) {
				super.func_178039_p();
			}
			
			mouseY += bottomLocation;
		}
	}
	
	/**
	 * Gui to display after this is closed.
	 */
	private final GuiScreen parent;
	/**
	 * List of mods.
	 */
	private ModList list;
	/**
	 * Details on the selected mod.
	 */
	private ModDetailList details;
	
	public GuiWDLExtensions(GuiScreen parent) {
		this.parent = parent;
	}
	
	@Override
	public void initGui() {
		bottomLocation = height - 100;
		dragging = false;
		
		this.list = new ModList();
		this.details = new ModDetailList();
		
		this.buttonList.add(new GuiButton(0, width / 2 - 100, height - 30, "Done"));
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 0) {
			mc.displayGuiScreen(parent);
		}
	}
	
	/**
	 * Whether the center section is being dragged.
	 */
	private boolean dragging = false;
	private int dragOffset;
	
	/**
	 * Handles mouse input.
	 */
	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		this.list.func_178039_p();
		this.details.func_178039_p();
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
			throws IOException {
		if (mouseY > bottomLocation && mouseY < bottomLocation + MIDDLE_HEIGHT) {
			dragging = true;
			dragOffset = mouseY - bottomLocation;
			
			return;
		}
		
		if (list.func_148179_a(mouseX, mouseY, mouseButton)) {
			return;
		}
		if (details.func_148179_a(mouseX, mouseY, mouseButton)) {
			return;
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		dragging = false;
		
		if (list.func_148181_b(mouseX, mouseY, state)) {
			return;
		}
		if (details.func_148181_b(mouseX, mouseY, state)) {
			return;
		}
		super.mouseReleased(mouseX, mouseY, state);
	}
	
	@Override
	protected void mouseClickMove(int mouseX, int mouseY,
			int clickedMouseButton, long timeSinceLastClick) {
		if (dragging) {
			bottomLocation = mouseY - dragOffset; 
		}
		
		//Clamp bottomLocation.
		if (bottomLocation < TOP_HEIGHT + 8) {
			bottomLocation = TOP_HEIGHT + 8;
		}
		if (bottomLocation > height - BOTTOM_HEIGHT - 8) {
			bottomLocation = height - BOTTOM_HEIGHT - 8;
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		
		//Clamp bottomLocation.
		if (bottomLocation < TOP_HEIGHT + 32) {
			bottomLocation = TOP_HEIGHT + 32;
		}
		if (bottomLocation > height - MIDDLE_HEIGHT - BOTTOM_HEIGHT - 32) {
			bottomLocation = height - MIDDLE_HEIGHT - BOTTOM_HEIGHT - 32;
		}
		
		this.list.drawScreen(mouseX, mouseY, partialTicks);
		this.details.drawScreen(mouseX, mouseY, partialTicks);
		
		this.drawCenteredString(this.fontRendererObj, "WDL extensions",
				this.width / 2, 8, 0xFFFFFF);
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
