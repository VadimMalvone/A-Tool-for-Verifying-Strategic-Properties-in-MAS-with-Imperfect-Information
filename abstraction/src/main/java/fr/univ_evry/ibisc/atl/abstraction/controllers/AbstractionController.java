package fr.univ_evry.ibisc.atl.abstraction.controllers;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

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

	
	@GetMapping("/")
	public String transformForm(Model model) {
		TransformBean transformBean = new TransformBean();
		transformBean.setAtlModel(AbstractionUtils.readSampleFile());
		model.addAttribute("transformBean", transformBean);
		return INDEX_PAGE;
	}

	@PostMapping("/")
	public String transformSubmit(@ModelAttribute TransformBean transformBean, Model model) throws Exception {
		try {
			if (StringUtils.isBlank(transformBean.getAtlModel())) {
				throw new Exception("Please provide an input json model to transform!");
			}
			AtlModel atlModel = JsonObject.load(transformBean.getAtlModel(), AtlModel.class);

			AbstractionUtils.validateAtlModel(atlModel);
			AbstractionUtils.processDefaultTransitions(atlModel);
			List<StateCluster> stateClusters = AbstractionUtils.getStateClusters(atlModel);
	    	List<Transition> mustTransitions = AbstractionUtils.getMustTransitions(atlModel, stateClusters);
	    	List<Transition> mayTransitions  = AbstractionUtils.getMayTransitions(atlModel, stateClusters);
	    	
	    	List<State> states = stateClusters.stream().map(StateCluster::toState).collect(Collectors.toList());
	    	
	    	AtlModel mustAtlModel = JsonObject.load(transformBean.getAtlModel(), AtlModel.class);
	    	mustAtlModel.setStates(states);
	    	mustAtlModel.setTransitions(mustTransitions);
	    	transformBean.setMustAtlModel(mustAtlModel.toString());

	    	AtlModel mayAtlModel = JsonObject.load(transformBean.getAtlModel(), AtlModel.class);
	    	mayAtlModel.setStates(states);
	    	mayAtlModel.setTransitions(mayTransitions);
	    	transformBean.setMayAtlModel(mayAtlModel.toString());
	    	
	    	transformBean.setDotAtlModel(AbstractionUtils.generateDotGraph(atlModel));
	    	transformBean.setDotMustAtlModel(AbstractionUtils.generateDotGraph(mustAtlModel));
	    	transformBean.setDotMayAtlModel(AbstractionUtils.generateDotGraph(mayAtlModel));

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

			String mayMcmasProgram = AbstractionUtils.generateMCMASProgram(mayAtlModel, true);
			transformBean.setMcmasMayAtlModel(mayMcmasProgram);

			logger.info(mayMcmasProgram);
			fileName = "/tmp/may" + System.currentTimeMillis()+".ispl";
			while (Files.exists(Paths.get(fileName))) {
				fileName = "/tmp/may" + System.currentTimeMillis()+".ispl";
			}
			Files.write(Paths.get(fileName), mayMcmasProgram.getBytes());
			String mcmasOutputMayAtlModel = AbstractionUtils.modelCheck(fileName);
			transformBean.setMcmasOutputMayAtlModel(mcmasOutputMayAtlModel);
			logger.info(AbstractionUtils.getMcmasResult(mcmasOutputMayAtlModel));


		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			String error = StringUtils.isNotBlank(e.getMessage())?e.getMessage():ExceptionUtils.getStackTrace(e);
			model.addAttribute("error", error);
		}
    	
		return INDEX_PAGE;
	}
	
}