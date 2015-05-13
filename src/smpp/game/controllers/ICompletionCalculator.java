package smpp.game.controllers;

import java.util.List;

import com.jme3.math.Vector3f;

public interface ICompletionCalculator {

	public float calculateCompletion(int index, List<Vector3f> positions, Vector3f pos);
}
