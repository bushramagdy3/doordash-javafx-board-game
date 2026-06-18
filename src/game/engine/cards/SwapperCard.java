package game.engine.cards;

import game.engine.monsters.Monster;

public class SwapperCard extends Card {
	
	public SwapperCard(String name, String description, int rarity){
		super(name, description, rarity, true);
		//Calling super means calling Card constructor with changed attributes.
	}
	
	@Override 
	public void performAction(Monster player, Monster opponent){
		int pos1 = player.getPosition();
		int pos2 = opponent.getPosition();
		if(pos1 < pos2){
			player.setPosition(pos2);
		    opponent.setPosition(pos1);
		}
		
		
	}
	
}
