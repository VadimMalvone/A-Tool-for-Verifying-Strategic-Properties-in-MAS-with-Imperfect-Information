package fr.univ_evry.ibisc.atl.abstraction.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import fr.univ_evry.ibisc.atl.abstraction.AbstractionUtils;
import fr.univ_evry.ibisc.atl.abstraction.beans.AtlModel;
import fr.univ_evry.ibisc.atl.abstraction.beans.JsonObject;
import fr.univ_evry.ibisc.atl.abstraction.beans.State;
import fr.univ_evry.ibisc.atl.abstraction.beans.StateCluster;
import fr.univ_evry.ibisc.atl.abstraction.beans.TransformBean;
import fr.univ_evry.ibisc.atl.abstraction.beans.Transition;

@Controller
public class AbstractionController {

	private static final String INDEX_PAGE = "index";
    private final Log logger = LogFactory.getLog(getClass());
	private AtlModel mustAtlModel;
	private AtlModel mayAtlModel;
	private AtlModel atlModel;


	@GetMapping("/")
	public String transformForm(Model model) {
		TransformBean transformBean = new TransformBean();
		transformBean.setAtlModel(AbstractionUtils.readSampleFile());
		model.addAttribute("transformBean", transformBean);
		return INDEX_PAGE;
	}

	@PostMapping("/Refine")
	public String handleRequest(@ModelAttribute TransformBean transformBean, Model model) throws IOException {
		AbstractionUtils.refinement(mustAtlModel, atlModel, (StateCluster) mustAtlModel.getStates().get(1), AbstractionUtils.Abstraction.Must);
		AbstractionUtils.refinement(mayAtlModel, atlModel, (StateCluster) mayAtlModel.getStates().get(1), AbstractionUtils.Abstraction.May);
		transformBean.setMustAtlModel(mustAtlModel.toString());
		transformBean.setMayAtlModel(mayAtlModel.toString());
		transformBean.setDotMustAtlModel(AbstractionUtils.generateDotGraph(mustAtlModel));
		transformBean.setDotMayAtlModel(AbstractionUtils.generateDotGraph(mayAtlModel));
		run(transformBean);
		return INDEX_PAGE;
	}

	private void run(@ModelAttribute TransformBean transformBean) throws IOException {
		String mustMcmasProgram = AbstractionUtils.generateMCMASProgram(mustAtlModel, false);
		transformBean.setMcmasMustAtlModel(mustMcmasProgram);
		logger.info(mustMcmasProgram);
		String fileName = "/tmp/must" + System.currentTimeMillis()+".ispl";
		while (Files.exists(Paths.get(fileName))) {
			fileName = "/tmp/must" + System.currentTimeMillis()+".ispl";
		}
		Files.write(Paths.get(fileName), mustMcmasProgram.getBytes());
		String mcmasOutputMustAtlModel = AbstractionUtils.modelCheck(fileName);
		transformBean.setMcmasOutputMustAtlModel(mcmasOutputMustAtlModel);
		logger.info(AbstractionUtils.getMcmasResult(mcmasOutputMustAtlModel));

		if(AbstractionUtils.getMcmasResult(mcmasOutputMustAtlModel)) {
			transformBean.setResult(true);
		} else {
			String mayMcmasProgram = AbstractionUtils.generateMCMASProgram(mayAtlModel, true);
			transformBean.setMcmasMayAtlModel(mayMcmasProgram);

			logger.info(mayMcmasProgram);
			fileName = "/tmp/may" + System.currentTimeMillis() + ".ispl";
			while (Files.exists(Paths.get(fileName))) {
				fileName = "/tmp/may" + System.currentTimeMillis() + ".ispl";
			}
			Files.write(Paths.get(fileName), mayMcmasProgram.getBytes());
			String mcmasOutputMayAtlModel = AbstractionUtils.modelCheck(fileName);
			transformBean.setMcmasOutputMayAtlModel(mcmasOutputMayAtlModel);
			logger.info(AbstractionUtils.getMcmasResult(mcmasOutputMayAtlModel));
			if(!AbstractionUtils.getMcmasResult(mcmasOutputMayAtlModel)) {
				transformBean.setResult(false);
			}
		}
	}

	@PostMapping("/Transform")
	public String transformSubmit(@ModelAttribute TransformBean transformBean, Model model) throws Exception {
		try {
			if (StringUtils.isBlank(transformBean.getAtlModel())) {
				throw new Exception("Please provide an input json model to transform!");
			}
			atlModel = JsonObject.load(transformBean.getAtlModel(), AtlModel.class);

			AbstractionUtils.validateAtlModel(atlModel);
			AbstractionUtils.processDefaultTransitions(atlModel);
			List<StateCluster> mustStateClusters = AbstractionUtils.getStateClusters(atlModel);
			List<StateCluster> mayStateClusters = AbstractionUtils.getStateClusters(atlModel);
	    	List<Transition> mustTransitions = AbstractionUtils.getMustTransitions(atlModel, mustStateClusters);
	    	List<Transition> mayTransitions  = AbstractionUtils.getMayTransitions(atlModel, mayStateClusters);
	    	
	    	//List<State> states = stateClusters.stream().map(StateCluster::toState).collect(Collectors.toList());
	    	
	    	mustAtlModel = JsonObject.load(transformBean.getAtlModel(), AtlModel.class);
	    	mustAtlModel.setStates(mustStateClusters);
	    	mustAtlModel.setTransitions(mustTransitions);
	    	mustAtlModel.setATL(atlModel.getATL().transl(true));
	    	transformBean.setMustAtlModel(mustAtlModel.toString());

	    	mayAtlModel = JsonObject.load(transformBean.getAtlModel(), AtlModel.class);
	    	mayAtlModel.setStates(mayStateClusters);
	    	mayAtlModel.setTransitions(mayTransitions);
			mayAtlModel.setATL(atlModel.getATL().transl(false));
	    	transformBean.setMayAtlModel(mayAtlModel.toString());
	    	
	    	transformBean.setDotAtlModel(AbstractionUtils.generateDotGraph(atlModel));
	    	transformBean.setDotMustAtlModel(AbstractionUtils.generateDotGraph(mustAtlModel));
	    	transformBean.setDotMayAtlModel(AbstractionUtils.generateDotGraph(mayAtlModel));

			run(transformBean);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			String error = StringUtils.isNotBlank(e.getMessage())?e.getMessage():ExceptionUtils.getStackTrace(e);
			model.addAttribute("error", error);
		}
    	
		return INDEX_PAGE;
	}
	
}