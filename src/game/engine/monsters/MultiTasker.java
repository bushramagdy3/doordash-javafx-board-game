package game.engine.monsters;

import game.engine.Constants;
import game.engine.Role;

public class MultiTasker extends Monster {
	private int normalSpeedTurns;

	public MultiTasker(String name, String description, Role role, int energy) {
		super(name, description,role,energy);
		this.normalSpeedTurns = 0;
	}
	
	public int getNormalSpeedTurns(){
		return normalSpeedTurns;
	}

	public void setNormalSpeedTurns(int normalSpeedTurns) {
		this.normalSpeedTurns = normalSpeedTurns;
	}

	@Override
	public void executePowerupEffect(Monster opponentMonster) {
	    this.setNormalSpeedTurns(2);
	}
	
	@Override
	public void move(int distance) {
	    if (getNormalSpeedTurns() > 0) {
	        super.move(distance);
	        setNormalSpeedTurns(getNormalSpeedTurns() - 1);
	    } else {
	        super.move(distance / 2);
	    }
	}
	
	@Override
	public void setEnergy(int energy) {
	    int delta = energy - getEnergy();
	    super.setEnergy(getEnergy() + delta + Constants.MULTITASKER_BONUS);
	}
	

}
