package org.intermine.objectstore.intermine;

/*
 * Copyright (C) 2002-2005 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.util.Properties;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import org.intermine.sql.Database;
import org.intermine.sql.DatabaseFactory;
import org.intermine.metadata.Model;
import org.intermine.metadata.MetadataManager;
import org.intermine.util.PropertiesUtil;

/**
 * Store model metadata to a database
 * @author Kim Rutherford
 */
public class StoreMetadataTask extends Task
{
    protected String modelName;
    protected String database;

    /**
     * Sets the model name
     *
     * @param modelName the model name
     */
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    /**
     * Sets the database alias
     * @param database the database alias
     */
    public void setDatabase(String database) {
        this.database = database;
    }

    /**
     * @see Task#execute
     */
    public void execute() throws BuildException {
        if (modelName == null) {
            throw new BuildException("modelName attribute is not set");
        }
        if (database == null) {
            throw new BuildException("database attribute is not set");
        }
        try {
            Database db = DatabaseFactory.getDatabase(database);

            Model model = MetadataManager.loadModel(modelName);
            MetadataManager.store(db, MetadataManager.MODEL, model.toString());

            Properties keys = MetadataManager.loadKeyDefinitions(modelName);
            if (keys != null) {
                MetadataManager.store(db, MetadataManager.KEY_DEFINITIONS,
                                      PropertiesUtil.serialize(keys));
            }

            Properties descriptions = MetadataManager.loadClassDescriptions(modelName);
            if (descriptions != null) {
                MetadataManager.store(db, MetadataManager.CLASS_DESCRIPTIONS,
                                      PropertiesUtil.serialize(descriptions));
            }
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }
}
