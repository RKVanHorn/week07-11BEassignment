package projects.service;

import java.util.List;
import java.util.NoSuchElementException;
import projects.dao.ProjectDao;
import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;

public class ProjectService {
	
	private ProjectDao projectDao = new ProjectDao();

	public Project addProject(Project project) {
		return projectDao.insertProject(project);
	}

	public List<Project> fetchAllProjects() {
		return projectDao.fetchAllProjects();
	}

	public Project fetchProjectById(Integer projectId) {
		return projectDao.fetchProjectById(projectId).orElseThrow(
				()-> new NoSuchElementException(
						"Project with project ID= " + projectId + " does not exist."));
		
	}

	public void modifyProjectDetails(Project project) {
		if(!projectDao.modifyProjectDetails(project)) {
			throw new DbException("Project with ID = " + project.getProjectId() + " does not exist.");
		}
		
	}

	public void addMaterial(Material material) {
		projectDao.addMaterialToProject(material);
		
	}

	public void addStep(Step step) {
		projectDao.addStepToProject(step);
		
	}

	public List<Category> fetchCategories() {
		return projectDao.fetchAllCategories();
	}

	public void addCategoryToProject(Integer projectId, String category) {
		projectDao.addCategoryToProject(projectId, category);
		
	}

	public void addCategoryToCategoryTable(String category) {
		projectDao.addCategoryToCategoryTable(category);
		
	}

	public void deleteProject(Integer projectId) {
		if(!projectDao.deleteProject(projectId)) {
			throw new DbException("Project with ID= " + projectId + " does not exist.");
		}
		
	}

	public List<Material> fetchMaterials(Integer projectId) {
		return projectDao.fetchProjectMaterials(projectId);
	}

	public void updateMaterialInSelectedProject(Material material) {
		if(!projectDao.updateMaterialInSelectedProject(material)) {
			throw new DbException("Material with ID= " + material.getMaterialId() + " does not exist.");
		}
	}

	public List<Step> fetchSteps(Integer projectId) {
		return projectDao.fetchProjectSteps(projectId);
	}

	public void updateStepInCurrentProject(Step step) {
		if(!projectDao.updateStepInCurrentProject(step)) {
			throw new DbException("Step with ID= " + step.getStepId() + " does not exist.");
		}
		
	}

	public void deleteMaterialFromCurrentProject(Integer materialId) {
		if(!projectDao.deleteMaterialFromCurrentProject(materialId)) {
			throw new DbException("Material with ID= " + materialId + " does not exist.");
		}
		
	}

	public void deleteStepFromCurrentProject(Integer stepId) {
		if(!projectDao.deleteStepFromCurrentProject(stepId)) {
			throw new DbException("Step with ID= " + stepId + " does not exist.");
		}
	}

	public void deleteCategoryFromCurrentProject(Integer projectId, Integer categoryId) {
		if(!projectDao.deleteCategoryFromCurrentProject(projectId, categoryId)) {
			throw new DbException(categoryId + " is not a category in Project ID= " + projectId );
		}
		
	}

	public List<Category> fetchCategoriesInAProject(Integer projectId) {
		return projectDao.fetchCategoriesInAProject(projectId);
	}

	
}
