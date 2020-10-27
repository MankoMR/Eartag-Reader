/*
 * Copyright (c) 2020. Manuel Koloska, Band Genossenschaft. All rights reserved.
 */

package ch.band.manko.eartagreader.models;

import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

class Module {
	public String name;
	public String url;
	public String version;
}
class LicenseGroup{
	public String licenseName;
	public String licenseUrl;
	public List<Module> modules;
}
class LicenseLoader {
	private List<LicenseGroup> licences = null;
	private OnSuccessListener<List<LicenseGroup>> listener;
	public LicenseLoader(OnSuccessListener<List<LicenseGroup>> listener, boolean loadNow){
		this.listener = listener;
		if(loadNow){
			load();
		}
	}
	public List<LicenseGroup> getLicences(){
		if(licences == null){
			load();
		}
		return  licences;
	}
	private void load(){
		licences = new ArrayList<>();
		listener.onSuccess(licences);
	}
}