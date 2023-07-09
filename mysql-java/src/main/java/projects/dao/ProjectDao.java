package projects.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;
import provided.util.DaoBase;

public class ProjectDao extends DaoBase{
	private static final String CATEGORY_TABLE = "category";
	private static final String MATERIAL_TABLE = "material";
	private static final String PROJECT_TABLE = "project";
	private static final String PROJECT_CATEGORY_TABLE = "project_category";
	private static final String STEP_TABLE = "step";
	

	public Project insertProject(Project project) {
		// @formatter:off
		String sql = ""
			+ "INSERT INTO " + PROJECT_TABLE + " "
			+ "(project_name, estimated_hours, actual_hours, difficulty, notes) "
			+ "VALUES "
			+"(?, ?, ?, ?, ?)";		
		// @formatter:on
		
		try(Connection conn = DbConnection.getConnection()){
			startTransaction(conn);
			try(PreparedStatement stmt = conn.prepareStatement(sql)){
				setParameter(stmt, 1, project.getProjectName(), String.class);
				setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
				setParameter(stmt, 4, project.getDifficulty(), Integer.class);
				setParameter(stmt, 5, project.getNotes(), String.class);
				
				stmt.executeUpdate();
				Integer projectId = getLastInsertId(conn, PROJECT_TABLE);
				
				commitTransaction(conn);
				project.setProjectId(projectId);
				return project;
			}
			catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}			
		} 
		catch (SQLException e) {
			throw new DbException(e);
		}		
	}


	public List<Project> fetchAllProjects() {
		String sql = "SELECT * FROM " + PROJECT_TABLE + " ORDER BY project_id";
		
		try(Connection conn = DbConnection.getConnection()){
			startTransaction(conn);
			
			try(PreparedStatement stmt = conn.prepareStatement(sql)){
				
				try(ResultSet rs = stmt.executeQuery()){
					List<Project> projects = new LinkedList<>();
					
					while(rs.next()) {
						projects.add(extract(rs, Project.class));
					}
					return projects;
				}
			}
			catch(Exception e){
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
		catch(SQLException e) {
			throw new DbException(e);
		}		
	}


	public Optional<Project> fetchProjectById(Integer projectId) {
		String sql = "SELECT * FROM " + PROJECT_TABLE + " WHERE project_id = ?";
		
		try(Connection conn = DbConnection.getConnection()){
			startTransaction(conn);
			
			try{
				Project project = null;
				
				try(PreparedStatement stmt = conn.prepareStatement(sql)){
					setParameter(stmt, 1, projectId, Integer.class);
					
					try(ResultSet rs = stmt.executeQuery()){
						if(rs.next()) {
							project = extract(rs, Project.class);
						}
					}
				}
				
				if(Objects.nonNull(project)) {
				project.getMaterials().addAll(fetchMaterialsForProject(conn, projectId));
				project.getSteps().addAll(fetchStepsForProject(conn, projectId));
				project.getCategories().addAll(fetchCategoriesForProject(conn, projectId));
				}
				commitTransaction(conn);
				return Optional.ofNullable(project);				
			}
			catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}			
		} 
		catch (SQLException e) {
			throw new DbException(e);
		}
		
		
	}


	private List<Category> fetchCategoriesForProject(Connection conn, Integer projectId)throws SQLException {
		//@formatter:off
		String sql = ""
				+"SELECT c.* FROM " + CATEGORY_TABLE + " c "
				+"JOIN " + PROJECT_CATEGORY_TABLE + " pc USING (category_id) "
				+"WHERE project_id = ?";
		//@formatter:on
		
		try(PreparedStatement stmt = conn.prepareStatement(sql)){
			setParameter(stmt, 1, projectId, Integer.class);
			
			try(ResultSet rs = stmt.executeQuery()){
				List<Category> categories = new LinkedList<>();
				
				while(rs.next()) {
					categories.add(extract(rs, Category.class));
				}
				return categories;
			}
		}
	}


	private List<Step> fetchStepsForProject(Connection conn, Integer projectId) throws SQLException {
		//@formatter:off
		String sql = ""
				+"SELECT * FROM " + STEP_TABLE
				+" WHERE project_id = ?";
		//@formatter:on
		
		try(PreparedStatement stmt = conn.prepareStatement(sql)){
			setParameter(stmt, 1, projectId, Integer.class);
			
			try(ResultSet rs = stmt.executeQuery()){
				List<Step> steps = new LinkedList<>();
				
				while(rs.next()) {
					steps.add(extract(rs, Step.class));
				}
				return steps;
			}
		}
	}


	private List<Material> fetchMaterialsForProject(Connection conn, Integer projectId)throws SQLException {
		//@formatter:off
		String sql = ""
				+"SELECT * FROM " + MATERIAL_TABLE
				+" WHERE project_id = ?";				
		//@formatter:on
		
		try(PreparedStatement stmt = conn.prepareStatement(sql)){
			setParameter(stmt, 1, projectId, Integer.class);
			
			try(ResultSet rs = stmt.executeQuery()){
				List<Material> materials = new LinkedList<>();
				
				while(rs.next()) {
					materials.add(extract(rs, Material.class));
				}
				return materials;
			}
		}
	}


	public boolean modifyProjectDetails(Project project) {
		//@formatter:off
		String sql = ""
				+ "UPDATE " + PROJECT_TABLE + " SET "
				+ "project_name = ?, "
				+ "estimated_hours = ?, "
				+ "actual_hours = ?, "
				+ "difficulty = ?, "
				+ "notes = ? "
				+ "WHERE project_id = ?";
		//@formatter:on
		
		try(Connection conn = DbConnection.getConnection()){
			startTransaction(conn);
			
			try(PreparedStatement stmt = conn.prepareStatement(sql)){
				setParameter(stmt, 1, project.getProjectName(), String.class);
				setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
				setParameter(stmt, 4, project.getDifficulty(), Integer.class);
				setParameter(stmt, 5, project.getNotes(), String.class);
				setParameter(stmt, 6, project.getProjectId(), Integer.class);
				
				boolean updated = stmt.executeUpdate() == 1;
				commitTransaction(conn);
				
				return updated;
				
			}
			catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
		catch(SQLException e) {
			throw new DbException(e);
		}
	}


	public void addMaterialToProject(Material material) {
		String sql = "INSERT INTO " + MATERIAL_TABLE
				+ " (project_id, material_name, num_required, cost)"
				+ " VALUES(?, ?, ?, ?)";
		
		try(Connection conn = DbConnection.getConnection()){
			startTransaction(conn);
			
			try(PreparedStatement stmt = conn.prepareStatement(sql)){
				setParameter(stmt, 1, material.getProjectId(), Integer.class);
				setParameter(stmt, 2, material.getMaterialName(), String.class);
				setParameter(stmt, 3, material.getNumRequired(), Integer.class);
				setParameter(stmt, 4, material.getCost(), BigDecimal.class);
				
				stmt.executeUpdate();
				commitTransaction(conn);
				
			}
			catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
		catch(SQLException e) {
			throw new DbException(e);
		}
	}


	public void addStepToProject(Step step) {
		String sql = "INSERT INTO " + STEP_TABLE + " (project_id, step_order, step_text)"
				+ " VALUES (?, ?, ?)";
		
		try(Connection conn = DbConnection.getConnection()){
			startTransaction(conn);
			Integer order = getNextSequenceNumber(conn, step.getProjectId(), STEP_TABLE, "project_id");
			
			try(PreparedStatement stmt = conn.prepareStatement(sql)){
				setParameter(stmt, 1, step.getProjectId(), Integer.class);
				setParameter(stmt, 2, order, Integer.class);
				setParameter(stmt, 3, step.getStepText(), String.class);
				
				stmt.executeUpdate();
				commitTransaction(conn);				
			}
			catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
		catch(SQLException e) {
			throw new DbException(e);
		}
		
	}


	public List<Category> fetchAllCategories() {
		String sql = "SELECT * FROM " + CATEGORY_TABLE + " ORDER BY category_name";
		
		try(Connection conn = DbConnection.getConnection()){
			startTransaction(conn);
			
			try(PreparedStatement stmt = conn.prepareStatement(sql)){
				try(ResultSet rs = stmt.executeQuery()){
					List<Category> categories = new LinkedList<>();
					
					while(rs.next()) {
						categories.add(extract(rs, Category.class));
					}
					
					return categories;
				}
				
			}
			catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
		catch(SQLException e) {
			throw new DbException(e);
		}
	}


	public void addCategoryToProject(Integer projectId, String category) {
		String subQuery = "(SELECT category_id FROM " + CATEGORY_TABLE + " WHERE category_name = ?)";
		String sql = "INSERT INTO " + PROJECT_CATEGORY_TABLE + " (project_id, category_id) VALUES (?, " + subQuery + ")";
		
		try(Connection conn = DbConnection.getConnection()){
			startTransaction(conn);
			
			try(PreparedStatement stmt = conn.prepareStatement(sql)){
				setParameter(stmt, 1, projectId, Integer.class);
				setParameter(stmt, 2, category, String.class);
				
				stmt.executeUpdate();
				commitTransaction(conn);
			}
			catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
		catch(SQLException e) {
			throw new DbException(e);
		}
		
	}


	public void addCategoryToCategoryTable(String category) {
		String sql = "INSERT INTO " + CATEGORY_TABLE + " (category_name) VALUES (?)";
		
		try(Connection conn = DbConnection.getConnection()){
			startTransaction(conn);
			
			try(PreparedStatement stmt = conn.prepareStatement(sql)){
				setParameter(stmt, 1, category, String.class);
				
				stmt.executeUpdate();
				commitTransaction(conn);				
			}
			catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
		catch(SQLException e) {
			throw new DbException(e);
		}
		
	}


	public boolean deleteProject(Integer projectId) {
		String sql = "DELETE FROM " + PROJECT_TABLE + " WHERE project_id = ?";
		
		try(Connection conn = DbConnection.getConnection()){
			startTransaction(conn);
			
			try(PreparedStatement stmt = conn.prepareStatement(sql)){
				setParameter(stmt, 1, projectId, Integer.class);
				
				boolean deleted = stmt.executeUpdate() == 1;
				
				commitTransaction(conn);
				return deleted;
			}
			catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
		catch(SQLException e) {
			throw new DbException(e);
		}
	}


	public List<Material> fetchProjectMaterials(Integer projectId) {
		try(Connection conn = DbConnection.getConnection()){
			startTransaction(conn);
			
			try {
				List<Material> materials = fetchMaterialsForProject(conn, projectId);
				commitTransaction(conn);
				
				return materials;
			}
			catch(Exception e) {
				throw new DbException(e);
			}
		}
		catch(SQLException e) {
			throw new DbException(e);
		}
	}


	public boolean updateMaterialInSelectedProject(Material material) {
		String sql = "UPDATE " + MATERIAL_TABLE + " SET material_name = ?, num_required = ?, cost = ? WHERE material_id = ?";
		
		try(Connection conn = DbConnection.getConnection()){
			startTransaction(conn);
			
			try(PreparedStatement stmt = conn.prepareStatement(sql)){
				setParameter(stmt, 1, material.getMaterialName(), String.class);
				setParameter(stmt, 2, material.getNumRequired(), Integer.class);
				setParameter(stmt, 3, material.getCost(), BigDecimal.class);
				setParameter(stmt, 4, material.getMaterialId(), Integer.class);
				
				boolean updated = stmt.executeUpdate() == 1;
				commitTransaction(conn);
				
				return updated;
			}
			catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
		catch(SQLException e) {
			throw new DbException(e);
		}
	}


	public List<Step> fetchProjectSteps(Integer projectId) {
		try(Connection conn = DbConnection.getConnection()){
			startTransaction(conn);
			
			try {
				List<Step> steps = fetchStepsForProject(conn, projectId);
				commitTransaction(conn);
				
				return steps;
				
			}catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
			
		}catch(SQLException e) {
			throw new DbException(e);
		}
	}


	public boolean updateStepInCurrentProject(Step step) {
		String sql = "UPDATE " + STEP_TABLE + " SET step_text = ? WHERE step_id = ?";
		
		try(Connection conn = DbConnection.getConnection()){
			startTransaction(conn);
			
			try(PreparedStatement stmt = conn.prepareStatement(sql)){
				setParameter(stmt, 1, step.getStepText(), String.class);
				setParameter(stmt, 2, step.getStepId(), Integer.class);
				
				boolean updated = stmt.executeUpdate() == 1;
				commitTransaction(conn);
				
				return updated;
			}
			catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
			
		}catch(SQLException e) {
			throw new DbException(e);
		}
	}


	public boolean deleteMaterialFromCurrentProject(Integer materialId) {
		String sql = "DELETE FROM " + MATERIAL_TABLE + " WHERE material_id = ?";
		
		try(Connection conn = DbConnection.getConnection()){
			startTransaction(conn);
			
			try(PreparedStatement stmt = conn.prepareStatement(sql)){
				setParameter(stmt, 1, materialId, Integer.class);
				
				boolean deleted = stmt.executeUpdate() == 1;
				
				commitTransaction(conn);
				return deleted;
			}
			catch(Exception e) {
				throw new DbException(e);
			}
		}
		catch(SQLException e) {
			throw new DbException(e);
		}
	}


	public boolean deleteStepFromCurrentProject(Integer stepId) {
		String sql = "DELETE FROM " + STEP_TABLE + " WHERE step_id = ?";
		
		try(Connection conn = DbConnection.getConnection()){
			startTransaction(conn);
			
			try(PreparedStatement stmt = conn.prepareStatement(sql)){
				setParameter(stmt, 1, stepId, Integer.class);
				
				boolean deleted = stmt.executeUpdate() == 1;
				
				commitTransaction(conn);
				return deleted;
			}
			catch(Exception e) {
				throw new DbException(e);
			}
		}
		catch(SQLException e) {
			throw new DbException(e);
		}
	}


	public boolean deleteCategoryFromCurrentProject(Integer projectId, Integer categoryId) {
		String sql = "DELETE FROM " + PROJECT_CATEGORY_TABLE + " WHERE project_id = ? AND category_id = ?";
		
		try(Connection conn = DbConnection.getConnection()){
			startTransaction(conn);
			
			try(PreparedStatement stmt = conn.prepareStatement(sql)){
				setParameter(stmt, 1, projectId, Integer.class);
				setParameter(stmt, 2, categoryId, Integer.class);
				
				boolean deleted = stmt.executeUpdate() == 1;
				
				commitTransaction(conn);
				return deleted;				
			}
			catch(Exception e) {
				throw new DbException(e);
			}
		}
		catch(SQLException e) {
			throw new DbException(e);
		}
	}


	public List<Category> fetchCategoriesInAProject(Integer projectId) {
		String sql = "SELECT * FROM " + CATEGORY_TABLE + " c JOIN " + PROJECT_CATEGORY_TABLE + " pc ON c.category_id = pc.category_id WHERE pc.project_id = ?";

		try(Connection conn = DbConnection.getConnection()){
			startTransaction(conn);
			
			try(PreparedStatement stmt = conn.prepareStatement(sql)){
				setParameter(stmt, 1, projectId, Integer.class);
				
				try(ResultSet rs = stmt.executeQuery()){
					List<Category> categories = new LinkedList<>();
					
					while(rs.next()) {
						categories.add(extract(rs, Category.class));
					}
					
					return categories;
				}
				
			}
			catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
		catch(SQLException e) {
			throw new DbException(e);
		}
	}
}
