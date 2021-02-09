package teamproject.wipeout.engine.component.physics;

import java.util.ArrayList;
import java.util.Arrays;

import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.entity.GameEntity;

public class MassEnergyComponent implements GameComponent {

	private double mass;
	
	//This constructor is for platforms, walls and other entities whose velocity should not change upon collision
	public MassEnergyComponent() {
		this.mass = Double.POSITIVE_INFINITY;
	}
	
	public MassEnergyComponent(double mass) {
		this.mass=mass;
	}
	
	public double getMass() {
		return this.mass;
	}

	@Override
	public String getType()
	{
		// TODO Auto-generated method stub
		return "mass_energy";
	}
   
    
}

