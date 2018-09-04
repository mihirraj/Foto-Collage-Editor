package com.wisesharksoftware.service.base;

import java.util.ArrayList;
import java.util.LinkedList;

import android.util.Log;

import com.wisesharksoftware.app_photoeditor.ChooseProcessingActivity;
import com.wisesharksoftware.panels.PanelManager;
import com.wisesharksoftware.service.AnimalEyesService;
import com.wisesharksoftware.service.BlurService;
import com.wisesharksoftware.service.BrightnessCurveService;
import com.wisesharksoftware.service.BrightnessService;
import com.wisesharksoftware.service.CombinePhotosService;
import com.wisesharksoftware.service.CropService;
import com.wisesharksoftware.service.EffectsService;
import com.wisesharksoftware.service.FaceService;
import com.wisesharksoftware.service.FisheyeService;
import com.wisesharksoftware.service.FocusService;
import com.wisesharksoftware.service.FrameService;
import com.wisesharksoftware.service.GraffitiService;
import com.wisesharksoftware.service.GrainService;
import com.wisesharksoftware.service.HDRToolService;
import com.wisesharksoftware.service.HighLightsService;
import com.wisesharksoftware.service.LensService;
import com.wisesharksoftware.service.ModesService;
import com.wisesharksoftware.service.NoCropFramesService;
import com.wisesharksoftware.service.RotateMirrorService;
import com.wisesharksoftware.service.SaturationService;
import com.wisesharksoftware.service.ShadowService;
import com.wisesharksoftware.service.ShapeService;
import com.wisesharksoftware.service.StickerService;
import com.wisesharksoftware.service.TemperatureService;
import com.wisesharksoftware.service.WandService;

public class ServicesManager {

	private static ServicesManager servicesManager;
	private static ArrayList<IService> services;
	private static LinkedList<IService> servicesQueue;
	private static final int PROCESS_ORDER_PRIORITY = 500;
	private IService currentService;

	public IService getCurrentService() {
		return currentService;
	}

	public static ArrayList<IService> getServices() {
		return services;
	}

	public void setCurrentService(IService currentService) {
		for (IService s : services) {
			if (s.getActionGroup().equals(currentService.getActionGroup())
					&& !s.equals(currentService)) {
				s.clear();
			}
		}
		this.currentService = currentService;
	};
	
	public static LinkedList<IService> getServicesQueueHighPriority() {
		LinkedList<IService> servicesQueuePriority = new LinkedList<IService>();
		for (int i = 0; i < servicesQueue.size(); i++) {
			if (servicesQueue.get(i).getPriority() >= PROCESS_ORDER_PRIORITY) {
				servicesQueuePriority.add(servicesQueue.get(i));
			}
		}
		return servicesQueuePriority;
	}
	
	public static LinkedList<IService> getServicesQueueLowPriority() {
		LinkedList<IService> servicesQueuePriority = new LinkedList<IService>();
		for (int i = 0; i < servicesQueue.size(); i++) {
			if (servicesQueue.get(i).getPriority() < PROCESS_ORDER_PRIORITY) {
				servicesQueuePriority.add(servicesQueue.get(i));
			}
		}
		return servicesQueuePriority;
	}

	public static LinkedList<IService> getServicesQueue() {
		return servicesQueue;
	}

	public static void addToQueue(IService s) {
		if (servicesQueue.contains(s)) {
			return;
		}
		int index = -1;
		int priority = s.getPriority();
		if (servicesQueue.size() == 0) {
			servicesQueue.add(s);
		} else {
			for (int i = 0; i < servicesQueue.size(); i++) {
				IService serv = servicesQueue.get(i);
				if (priority == serv.getPriority()) {
					if (i + 1 != servicesQueue.size()) {
						continue;
					} else {
						servicesQueue.add(s);
						break;
					}
				} else if (priority < serv.getPriority()) {
					if (i + 1 != servicesQueue.size()) {
						continue;
					} else {
						servicesQueue.add(s);
						break;
					}
				} else if (priority > serv.getPriority()) {
					index = i;
					break;
				}
			}
			if (index != -1) {
				servicesQueue.add(index, s);
			}
		}

	}

	public static ServicesManager instance() {
		if (servicesManager == null) {
			servicesManager = new ServicesManager();
			services = new ArrayList<IService>();
			servicesQueue = new LinkedList<IService>();
		}
		if (services == null) {
			services = new ArrayList<IService>();
		}
		if (servicesQueue == null) {
			servicesQueue = new LinkedList<IService>();
		}
		return servicesManager;
	}

	public IService getService(ChooseProcessingActivity a, PanelManager m,
			String action, String actionGroup) {
		IService s = null;
		for (IService serv : services) {
			if (serv.getActionGroup().equals(actionGroup)
					&& serv.getAction().equals(action)) {
				return serv;
			}
		}
		if (action.equals("sticker")) {

			if (actionGroup.equals("shapes")) {
				s = new ShapeService(a, m, action, actionGroup);
			} else if (actionGroup.equals("frames")) {
				s = new FrameService(a, m, action, actionGroup);
			} else if (actionGroup.equals("animaleyes")) {
				s = new AnimalEyesService(a, m, action, actionGroup);
			} else if (actionGroup.equals("graffiti")) {
				s = new GraffitiService(a, m, action, actionGroup);
			} else {
				s = new StickerService(a, m, action, actionGroup);
			}
		} else if (action.equals("effects")) {
			s = new EffectsService(a, m, action, actionGroup);
		} else if (action.equals("combinephotos")) {
			s = new CombinePhotosService(a, m, action, actionGroup);
		} else if (action.equals("focus")) {
			s = new FocusService(a, m, action, actionGroup);
		} else if (action.equals("crop")) {
			s = new CropService(a, m, action, actionGroup);
		} else if (action.equals("brightness")) {
			s = new BrightnessService(a, m, action, actionGroup);
		} else if (action.equals("brightness_curve")) {
			s = new BrightnessCurveService(a, m, action, actionGroup);
		} else if (action.equals("saturation")) {
			s = new SaturationService(a, m, action, actionGroup);
		} else if (action.equals("blur")) {
			s = new BlurService(a, m, action, actionGroup);
		} else if (action.equals("temperature")) {
			s = new TemperatureService(a, m, action, actionGroup);
		} else if (action.equals("grain")) {
			s = new GrainService(a, m, action, actionGroup);
		} else if (action.equals("shadow")) {
			s = new ShadowService(a, m, action, actionGroup);
		} else if (action.equals("nocropframes")) {
			s = new NoCropFramesService(a, m, action, actionGroup);
		} else if (action.equals("highlights")) {
			s = new HighLightsService(a, m, action, actionGroup);
		} else if (action.equals("wand")) {
			s = new WandService(a, m, action, actionGroup);
		} else if (action.equals("rotate_mirror")) {
			s = new RotateMirrorService(a, m, action, actionGroup);
		} else if (action.equals("lens")) {
			s = new LensService(a, m, action, actionGroup);
		} else if (action.equals("fisheye")) {
			s = new FisheyeService(a, m, action, actionGroup);
		} else if (action.equals("modes")) {
			s = new ModesService(a, m, action, actionGroup);
		} else if (action.equals("faces")) {
			s = new FaceService(a, m, action, actionGroup);
		} else if (action.equals("hdrtool")) {
			s = new HDRToolService(a, m, action, actionGroup);
		}
		if (s != null) {
			services.add(s);
		}
		return s;
	}

	public static void clear() {
		if (services != null)
			services.clear();
		services = null;
		if (servicesQueue != null)
			servicesQueue.clear();
		servicesQueue = null;
	}

	public static void clearServices() {
		if (services != null)
			for (IService s : services) {
				s.clear();
			}
		if (servicesQueue != null)
			servicesQueue.clear();
		servicesQueue = null;
		servicesQueue = new LinkedList<IService>();
	}
}
