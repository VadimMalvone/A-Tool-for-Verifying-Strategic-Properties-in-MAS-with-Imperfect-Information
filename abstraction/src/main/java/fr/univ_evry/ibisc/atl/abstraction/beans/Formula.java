package fr.univ_evry.ibisc.atl.abstraction.beans;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Formula extends JsonObject {

	@SerializedName("group")
	@Expose
	private String name;
	@SerializedName("sub-formula")
	@Expose
	private String subformula;
	@SerializedName("terms")
	@Expose
	private List<String> terms = new ArrayList<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSubformula() {
		return subformula;
	}

	public void setSubformula(String subformula) {
		this.subformula = subformula;
	}

	public List<String> getTerms() {
		return terms;
	}

	public void setTerms(List<String> terms) {
		this.terms = terms;
	}
}
