package com.promineotech.projects.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.promineotech.projects.entity.Category;
import com.promineotech.projects.entity.Material;
import com.promineotech.projects.entity.Project;
import com.promineotech.projects.entity.Step;
import com.promineotech.projects.exception.DbException;
import com.promineotech.provided.util.DaoBase;

public class ProjectDao extends DaoBase {

    // Constants for table names
    private static final String CATEGORY_TABLE = "category";
    private static final String MATERIAL_TABLE = "material";
    private static final String PROJECT_TABLE = "project";
    private static final String PROJECT_CATEGORY_TABLE = "project_category";
    private static final String STEP_TABLE = "step";

    /**
     * Inserts a new project into the database.
     * 
     * @param project The project object containing the details to insert.
     * @return The same project object with the generated project ID set.
     */
    public Project insertProject(Project project) {
        String sql = "INSERT INTO " + PROJECT_TABLE +
                " (project_name, estimated_hours, actual_hours, difficulty, notes) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DbConnection.getConnection()) {
            // Start transaction
            startTransaction(conn);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // Set parameters using DaoBase's utility method
                setParameter(stmt, 1, project.getProjectName(), String.class);
                setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
                setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
                setParameter(stmt, 4, project.getDifficulty(), Integer.class);
                setParameter(stmt, 5, project.getNotes(), String.class);

                // Execute the statement
                stmt.executeUpdate();

                // Get the ID of the newly inserted project
                Integer projectId = getLastInsertId(conn, PROJECT_TABLE);

                // Commit the transaction
                commitTransaction(conn);

                // Set the project ID in the project object and return it
                project.setProjectId(projectId);
                return project;
            } catch (Exception e) {
                // Roll back the transaction in case of an exception
                rollbackTransaction(conn);
                throw new DbException(e);
            }
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    public List<Project> fetchAllProjects() {
        String sql = "SELECT * FROM " + PROJECT_TABLE + " ORDER BY project_name";
        List<Project> projects = new ArrayList<>();

        try (Connection conn = DbConnection.getConnection()) {
            startTransaction(conn);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        projects.add(extract(rs, Project.class));
                        /*
                         * Project project = new Project();
                         * project.setProjectId(rs.getObject("project_id", Integer.class));
                         * project.setProjectName(rs.getString("project_name"));
                         * project.setEstimatedHours(rs.getBigDecimal("estimated_hours"));
                         * project.setActualHours(rs.getBigDecimal("actual_hours"));
                         * project.setDifficulty(rs.getObject("difficulty", Integer.class));
                         * project.setNotes(rs.getString("notes"));
                         * 
                         * projects.add(project);
                         */
                    }
                    commitTransaction(conn);
                }
            } catch (Exception e) {
                rollbackTransaction(conn);
                throw new DbException(e);
            }
        } catch (SQLException e) {
            throw new DbException(e);
        }
        return projects;
    }

    public Optional<Project> fetchProjectById(Integer projectId) {
        String sql = "SELECT * FROM " + PROJECT_TABLE + " WHERE project_id = ?";

        try (Connection conn = DbConnection.getConnection()) {
            startTransaction(conn);

            try {
                Project project = null;

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    setParameter(stmt, 1, projectId, Integer.class);

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            project = extract(rs, Project.class);
                        }
                    }
                }

                if (Objects.nonNull(project)) {
                    project.getMaterials().addAll(fetchMaterialsForProject(conn, projectId));
                    project.getSteps().addAll(fetchStepsForProject(conn, projectId));
                    project.getCategories().addAll(fetchCategoriesForProject(conn, projectId));
                }

                commitTransaction(conn);

                return Optional.ofNullable(project);
            } catch (Exception e) {
                rollbackTransaction(conn);
                throw new DbException(e);
            }
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    private List<Category> fetchCategoriesForProject(Connection conn, Integer projectId) {
    // @formatter:off
    String sql = ""
        + "SELECT c.* FROM " + CATEGORY_TABLE + " c "
        + "JOIN " + PROJECT_CATEGORY_TABLE + " pc USING (category_id) "
        + "WHERE project_id = ?";
    // @formatter:on

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameter(stmt, 1, projectId, Integer.class);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Category> categories = new LinkedList<>();

                while (rs.next()) {
                    categories.add(extract(rs, Category.class));
                }

                return categories;
            }
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    private List<Step> fetchStepsForProject(Connection conn, Integer projectId) throws SQLException {
        String sql = "SELECT * FROM " + STEP_TABLE + " WHERE project_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameter(stmt, 1, projectId, Integer.class);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Step> steps = new LinkedList<>();

                while (rs.next()) {
                    steps.add(extract(rs, Step.class));
                }

                return steps;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T extract(ResultSet rs, Class<T> clazz) throws SQLException {
        if (clazz == Project.class) {
            Project project = new Project();
            project.setProjectId(rs.getInt("project_id"));
            project.setProjectName(rs.getString("project_name"));
            project.setEstimatedHours(rs.getBigDecimal("estimated_hours"));
            project.setActualHours(rs.getBigDecimal("actual_hours"));
            project.setDifficulty(rs.getInt("difficulty"));
            project.setNotes(rs.getString("notes"));
            return (T) project;
        } else if (clazz == Category.class) {
            Category category = new Category();
            category.setCategoryId(rs.getInt("category_id"));
            category.setCategoryName(rs.getString("category_name"));

            return (T) category;
        } else if (clazz == Step.class) {
            Step step = new Step();
            step.setStepId(rs.getInt("step_id"));

            return (T) step;
        }
        throw new IllegalArgumentException("Unsupported class type: " + clazz.getName());
    }

    private List<Material> fetchMaterialsForProject(Connection conn, Integer projectId) throws SQLException {
        String sql = "SELECT * FROM " + MATERIAL_TABLE + " WHERE project_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameter(stmt, 1, projectId, Integer.class);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Material> materials = new LinkedList<>();

                while (rs.next()) {
                    Material material = new Material();
                    material.setMaterialId(rs.getInt("material_id"));

                    material.setMaterialName(rs.getString("material_name"));

                    materials.add(material);
                }
                return materials;
            }
        }
    }

    public boolean modifyProjectDetails(Project project) {
    // @formatter:off
    String sql = ""
    + "UPDATE " + PROJECT_TABLE + " SET "
    + "project_name = ?, "
    + "estimated_hours = ?, "
    + "actual_hours = ?, "
    + "difficulty = ?, "
    + "notes = ? "
    + "WHERE project_id = ?";
    // @formatter:on

        try (

                Connection conn = DbConnection.getConnection()) {
            startTransaction(conn);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                setParameter(stmt, 1, project.getProjectName(), String.class);
                setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
                setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
                setParameter(stmt, 4, project.getDifficulty(), Integer.class);
                setParameter(stmt, 5, project.getNotes(), String.class);
                setParameter(stmt, 6, project.getProjectId(), Integer.class);

                boolean modified = stmt.executeUpdate() == 1;
                commitTransaction(conn);

                return modified;
            } catch (Exception e) {
                rollbackTransaction(conn);
                throw new DbException(e);
            }
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    public boolean deleteProject(Integer projectId) {
        String sql = "DELETE FROM " + PROJECT_TABLE + " WHERE project_id = ?";

    try(Connection conn = DbConnection.getConnection()) {
      startTransaction(conn);

      try(PreparedStatement stmt = conn.prepareStatement(sql)) {
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
}
