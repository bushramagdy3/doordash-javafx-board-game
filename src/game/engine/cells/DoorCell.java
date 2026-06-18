package game.engine.cells;
import game.engine.Board;
import game.engine.Role;
import game.engine.interfaces.CanisterModifier;
import game.engine.monsters.*;

public class DoorCell extends Cell implements CanisterModifier{
	private Role role;
	private int energy;
	private boolean activated;

	public DoorCell(String name, Role role, int energy) {
		super(name);
		this.role = role;
		this.energy = energy;
		this.activated = false;
	}
	public Role getRole() {
		return role;
	}
	public int getEnergy() {
		return energy;
	}
	public boolean isActivated() {
		return activated;
	}
	public void setActivated(boolean activated) {
		this.activated = activated;
	}

	@Override
	public void modifyCanisterEnergy(Monster monster, int canisterValue) {
	    if (monster.getRole() == getRole())
	        monster.alterEnergy(canisterValue);
	    else
	        monster.alterEnergy(-canisterValue);
	}
	
	@Override
	public void onLand(Monster landingMonster, Monster opponentMonster) {
	    super.onLand(landingMonster, opponentMonster);
	    if (isActivated())
	        return;
	    if (landingMonster.getRole() != getRole() && landingMonster.isShielded()) {
	        landingMonster.setShielded(false);
	        return;
	    }
	    modifyCanisterEnergy(landingMonster, getEnergy());
	    for (Monster m : Board.getStationedMonsters())
	        if (m.getRole() == landingMonster.getRole())
	            modifyCanisterEnergy(m, getEnergy());
	    setActivated(true);
	}
}