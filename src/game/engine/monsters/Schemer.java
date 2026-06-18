package game.engine.monsters;

import game.engine.Board;
import game.engine.Constants;
import game.engine.Role;

public class Schemer extends Monster{
	public Schemer(String name, String description, Role role,int energy){
		super(name, description, role, energy);
	}

	@Override
	public void executePowerupEffect(Monster opponentMonster) {

	    int total = 0;

	    total += stealEnergyFrom(opponentMonster);

	    for (Monster m : game.engine.Board.getStationedMonsters()) {
	    	if(m != opponentMonster)
	    		total += stealEnergyFrom(m);
	    }

	    setEnergy(getEnergy() + total);
	}
	
	private int stealEnergyFrom(Monster target) {

	    int amount = Math.min(Constants.SCHEMER_STEAL, target.getEnergy());

	    target.setEnergy(target.getEnergy() - amount);

	    return amount;
	}
	
	@Override
	public void setEnergy(int energy) {
	    int delta = energy - getEnergy();
	    super.setEnergy(getEnergy() + delta + Constants.SCHEMER_STEAL);
	}
	
}
