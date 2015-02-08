package eu.crushedpixel.replaymod.interpolation;

import akka.japi.Pair;
import eu.crushedpixel.replaymod.holders.Position;

public class LinearPoint extends LinearInterpolation<Position> {

	@Override
	public Position getPoint(float positionIn) {
		Pair<Float, Pair<Position, Position>> pair = getCurrentPoints(positionIn);
		if(pair == null) return null;
		
		float perc = pair.first();
		//float position = positionIn * (points.size()-1);
		//int      cubicNum = (int)Math.min(points.size()-1, position);
		//float    perc = (position - cubicNum);
		
		//System.out.println(cubicNum+" | "+perc+" | "+positionIn);
		
		Position first = pair.second().first();
		Position second = pair.second().second();
		
		double x = getInterpolatedValue(first.getX(), second.getX(), perc);
		double y = getInterpolatedValue(first.getY(), second.getY(), perc);
		double z = getInterpolatedValue(first.getZ(), second.getZ(), perc);
		
		float pitch = (float)getInterpolatedValue(first.getPitch(), second.getPitch(), perc);
		float yaw = (float)getInterpolatedValue(first.getYaw(), second.getYaw(), perc);
		
		Position inter = new Position(x, y, z, pitch, yaw);
		//System.out.println(position+" | "+cubicNum+" | "+perc+" | "+first+" | "+second+" | "+inter);
		return inter;
	}
}
