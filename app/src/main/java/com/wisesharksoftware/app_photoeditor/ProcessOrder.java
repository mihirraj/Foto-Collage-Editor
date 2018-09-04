package com.wisesharksoftware.app_photoeditor;

import java.util.ArrayList;

import android.util.Log;

import com.wisesharksoftware.core.Filter;

public class ProcessOrder {
	public ArrayList<ItemOrder> order = new ArrayList<ItemOrder>();
	private static final int NONE_OPERATION = -1;
	private static final int ADD_ROTATE_FLIP_OPERATION = 0;
	private static final int ADD_FILTER_OPERATION = 2;
	private int current_operation = NONE_OPERATION;

	public ProcessOrder() {
		order.add(new ItemOrder());
	}

	public void addRotateFlip(int angle, boolean flipVertical,
			boolean flipHorizontal) {
		if (isSwitched(ADD_ROTATE_FLIP_OPERATION)) {
			order.add(new ItemOrder());
		}
		order.get(order.size() - 1).angle = angle;
		order.get(order.size() - 1).flipVertical = flipVertical;
		order.get(order.size() - 1).flipHorizontal = flipHorizontal;
	}

	public int addRotateLeft() {
		if (isSwitched(ADD_ROTATE_FLIP_OPERATION)) {
			order.add(new ItemOrder());
		}
		order.get(order.size() - 1).angle = (order.get(order.size() - 1).angle - 90) % 360;
		return order.get(order.size() - 1).angle;
	}

	public int addRotateRight() {
		if (isSwitched(ADD_ROTATE_FLIP_OPERATION)) {
			order.add(new ItemOrder());
		}
		order.get(order.size() - 1).angle = (order.get(order.size() - 1).angle + 90) % 360;
		int angle = order.get(order.size() - 1).angle;
		return angle;
	}

	public boolean addFlipHorizontal() {
		if (isSwitched(ADD_ROTATE_FLIP_OPERATION)) {
			order.add(new ItemOrder());
		}
		order.get(order.size() - 1).flipHorizontal = !(order
				.get(order.size() - 1).flipHorizontal);
		boolean flipHorizontal = order.get(order.size() - 1).flipHorizontal;
		return flipHorizontal;
	}

	public boolean addFlipVertical() {
		if (isSwitched(ADD_ROTATE_FLIP_OPERATION)) {
			order.add(new ItemOrder());
		}
		order.get(order.size() - 1).flipVertical = !(order
				.get(order.size() - 1).flipVertical);
		boolean flipVertical = order.get(order.size() - 1).flipVertical;
		return flipVertical;
	}

	public void addFilter(Filter filter, String type, boolean delete) {
		if (delete) {
			Log.d("DELETE", "TYPE = " + type);
			deleteOrder(type);
		}
		//if (isSwitched(ADD_FILTER_OPERATION)) {

			ItemOrder o = new ItemOrder();
			o.setType(type);
			order.add(o);
		//}
		order.get(order.size() - 1).filters.add(filter);
	}

	private void deleteOrder(String type) {
		int indexForDelete = -1;
		for (int i = 0; i < order.size(); i++) {
			ItemOrder o = order.get(i);
			if (o.getType() != null && o.getType().equals(type)) {
				indexForDelete = i;
				break;
			}
		}
		if (indexForDelete != -1) {
			order.remove(indexForDelete);
		}
	}

	public int size() {
		return order.size();
	}

	public boolean isSwitched(int newOperation) {
		if (current_operation == NONE_OPERATION) {
			current_operation = newOperation;
			Log.d("sticker", "1");
			return false;
		} else if (current_operation == newOperation) {
			current_operation = newOperation;
			Log.d("sticker", "2");
			return false;
		} else {
			current_operation = newOperation;
			Log.d("sticker", "3");
			return true;
		}
		// return true;
	}
}