package edu.asu.bsenetza.hateoas.gradebook.bsenetzacli.ui;

import com.sun.jersey.api.client.ClientResponse;
import edu.asu.bsenetza.hateoas.gradebook.bsenetzacli.Gradebook_HATEOAS_cl;
import edu.asu.bsenetza.hateoas.gradebook.bsenetzacli.jaxb.model.Grade;
import edu.asu.bsenetza.hateoas.gradebook.bsenetzacli.jaxb.model.GradedItem;
import edu.asu.bsenetza.hateoas.gradebook.bsenetzacli.jaxb.utils.Converter;
import edu.asu.bsenetza.hateoas.gradebook.bsenetzacli.representation.Link;
import static edu.asu.bsenetza.hateoas.gradebook.bsenetzacli.representation.Representation.*;
import java.lang.invoke.MethodHandles;

import javax.ws.rs.core.Response;

import javax.swing.JFrame;

import javax.xml.bind.JAXBException;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Gradebook_HATEOAS_UI extends JFrame {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Gradebook_HATEOAS_cl gradebook_HATEOAS_client;

    private URI resourceURI;

    /**
     * Creates new form Gradebook_HATEOAS_cl
     */
    public Gradebook_HATEOAS_UI() {
        LOG.info("Creating a Gradebook_CRUD_cl object");
        initComponents();
        reset();

        gradebook_HATEOAS_client = new Gradebook_HATEOAS_cl();
    }

    private String convertFormToXMLString() {
        GradedItem gradedItem = new GradedItem();
        if (!jTextField1.getText().trim().equals("")) {
            gradedItem.setGradedItemId(Integer.parseInt(jTextField1.getText()));
        }
        gradedItem.setDescription(jTextField2.getText());
        if (!jTextField4.getText().trim().equals("")) {
            gradedItem.setPercentage(Float.valueOf(jTextField4.getText()));
        }

        if (!jTextField7.getText().trim().equals("")) {
            Grade grade = new Grade();
            grade.setStudentId(Integer.parseInt(jTextField7.getText()));
            if (!jTextField8.getText().trim().equals("")) {
                grade.setScore(Float.valueOf(jTextField8.getText()));
            } else {
                grade.setScore(Float.NaN);
            }

            grade.setComment(jTextField9.getText());
            List<Grade> grades = new ArrayList<>();
            grades.add(grade);
            gradedItem.setGrade(grades);
        }

        LOG.debug(gradedItem.toString());
        String xmlString = Converter.convertFromObjectToXml(gradedItem, gradedItem.getClass());

        return xmlString;
    }

    private void populateForm(ClientResponse clientResponse) {
        LOG.info("Populating the UI with the gradedItem info");
        LOG.debug("clientResponse.getStatus() {}", clientResponse.getStatus());
        try {
            if ((clientResponse.getStatus() == Response.Status.OK.getStatusCode())
                    || (clientResponse.getStatus() == Response.Status.CREATED.getStatusCode())) {
                String entity = clientResponse.getEntity(String.class);
                LOG.debug("The Client Response entity is {}", entity);

                GradedItem gradedItem = (GradedItem) Converter.convertFromXmlToObject(entity, GradedItem.class);
                LOG.debug("The Client Response gradedItem object is {}", gradedItem);
                setLinks(gradedItem);
                enableState();

                // Populate Appointment info
                jTextField1.setText(String.valueOf(gradedItem.getGradedItemId()));
                jTextField2.setText(gradedItem.getDescription());
                jTextField4.setText(String.valueOf(gradedItem.getPercentage()));
                jTextField10.setText(entity);

                if ((!gradedItem.getGrade().isEmpty()) && (!jTextField7.getText().equals(""))
                        && (clientResponse.getStatus() != Response.Status.CREATED.getStatusCode())) {

                    LOG.debug("studentId {} ", jTextField7.getText());
                    LOG.debug("studentId {} ", Integer.parseInt(jTextField7.getText()));
                    LOG.debug("get grade {} for studentId {} ",
                            gradedItem.getGradeByStudentId(Integer.parseInt(jTextField7.getText())), jTextField7.getText());

                    if (Float.isNaN(gradedItem.getGradeByStudentId(Integer.parseInt(jTextField7.getText())).getScore())) {
                        jTextField8.setText("");
                    } else {
                        jTextField8.setText(String.valueOf(gradedItem.getGradeByStudentId(Integer.parseInt(jTextField7.getText())).getScore()));
                    }
                    jTextField9.setText(gradedItem.getGradeByStudentId(Integer.parseInt(jTextField7.getText())).getComment());
                }
            } else if ((clientResponse.getStatus() == Response.Status.NOT_FOUND.getStatusCode())
                    || (clientResponse.getStatus() == Response.Status.CONFLICT.getStatusCode())) {
                String entity = clientResponse.getEntity(String.class);
                LOG.debug("The Client Response entity is {}", entity);
                jTextField2.setText("");
                jTextField4.setText("");
                jTextField10.setText(entity);
                jRadioButton3.setEnabled(Boolean.FALSE);
                jRadioButton4.setEnabled(Boolean.FALSE);
            } else if (clientResponse.getStatus() == Response.Status.PRECONDITION_FAILED.getStatusCode()) {
                String entity = clientResponse.getEntity(String.class);
                LOG.debug("The Client Response entity is {}", entity);
                jTextField5.setText(clientResponse.getLocation().toString());
                jTextField10.setText(entity);
            } else {
                jTextField2.setText("");
                jTextField4.setText("");
            }

            // Populate HTTP Header Information
            jTextField3.setText(Integer.toString(clientResponse.getStatus()));
            if (clientResponse.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
                clearLinks();
                disableAll();
                jRadioButton1.setEnabled(Boolean.TRUE);
                jRadioButton2.setEnabled(Boolean.TRUE);
                jTextField6.setText("");
                jTextField10.setText("");
            } else {
                jTextField6.setText(clientResponse.getType().toString());
            }

            // The Location filed is only populated when a Resource is created
            if (clientResponse.getStatus() == Response.Status.CREATED.getStatusCode()) {
                jTextField5.setText(clientResponse.getLocation().toString());
            } else {
                jTextField5.setText("");
            }

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    private void enableState() {
        LOG.debug("enableState");

        boolean self_rel_value = false;
        boolean gradedItem_update_value = false;
        boolean gradedItem_delete_value = false;
        boolean grade_create_value = false;
        boolean grade_self_value = false;
        boolean grade_update_value = false;
        boolean grade_delete_value = false;

        disableAll();

        for (Link link : getLinks()) {
            LOG.debug("Link getUri, getRelValue {} {}", link.getUri().toString(), link.getRelValue());

            if (link.getRelValue().equals(RELATIONS_URI + SELF_REL_VALUE)) {
                self_rel_value = true;
            }
            if (link.getRelValue().equals(RELATIONS_URI + GRADEDITEM_UPDATE_VALUE)) {
                gradedItem_update_value = true;
            }
            if (link.getRelValue().equals(RELATIONS_URI + GRADEDITEM_DELETE_VALUE)) {
                gradedItem_delete_value = true;
            }
            if (link.getRelValue().equals(RELATIONS_URI + GRADE_CREATE_VALUE)) {
                grade_create_value = true;
            }
            if (link.getRelValue().equals(RELATIONS_URI + GRADE_SELF_VALUE)) {
                grade_self_value = true;
            }
            if (link.getRelValue().equals(RELATIONS_URI + GRADE_UPDATE_VALUE)) {
                grade_update_value = true;
            }
            if (link.getRelValue().equals(RELATIONS_URI + GRADE_DELETE_VALUE)) {
                grade_delete_value = true;
            }

        }
        if (self_rel_value) {
            jRadioButton2.setEnabled(Boolean.TRUE);
            enableGradedItemId();
        }
        if (gradedItem_update_value) {
            jRadioButton3.setEnabled(Boolean.TRUE);
            enableDescription();
            enablePercentage();
        }
        if (gradedItem_delete_value) {
            jRadioButton4.setEnabled(Boolean.TRUE);
            enableGradedItemId();
        }
        if (grade_create_value) {
            jRadioButton1.setEnabled(Boolean.TRUE);
            enableStudentId();
            enableScore();
            enableComment();
        }
        if (grade_self_value && self_rel_value) {
            jRadioButton2.setEnabled(Boolean.TRUE);
            enableStudentId();
        }
        if (grade_update_value) {
            jRadioButton3.setEnabled(Boolean.TRUE);
            disableStudentId();
            enableScore();
            enableComment();
        }
        if (grade_delete_value) {
            jRadioButton4.setEnabled(Boolean.TRUE);
            disableStudentId();
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jRadioButton4 = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jTextField6 = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jTextField7 = new javax.swing.JTextField();
        jTextField8 = new javax.swing.JTextField();
        jTextField9 = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jTextField10 = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        jLabel1.setText("Action");

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setText("Create");
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText("Read");
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton3);
        jRadioButton3.setText("Update");
        jRadioButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton3ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton4);
        jRadioButton4.setText("Delete");
        jRadioButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton4ActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        jLabel2.setText("Graded Item");

        jLabel3.setText("Graded Item Id");

        jLabel4.setText("Description");

        jLabel6.setText("Percentage");

        jTextField1.setToolTipText("Integer");
        jTextField1.setName("IdField"); // NOI18N
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jTextField2.setToolTipText("Text");
        jTextField2.setName("TitleField"); // NOI18N
        jTextField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField2ActionPerformed(evt);
            }
        });

        jTextField4.setToolTipText("Decimal");
        jTextField4.setName("PriorityField"); // NOI18N
        jTextField4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField4ActionPerformed(evt);
            }
        });

        jButton1.setText("Submit");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel5.setText("HTTP Status Code");

        jTextField3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField3ActionPerformed(evt);
            }
        });

        jLabel7.setText("Location");

        jLabel8.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        jLabel8.setText("HTTP Header Info");

        jTextField5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField5ActionPerformed(evt);
            }
        });

        jLabel9.setText("Media Type");

        jTextField6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField6ActionPerformed(evt);
            }
        });

        jLabel10.setText("Student Id");

        jLabel11.setText("Score");

        jLabel12.setText("Comment");

        jTextField7.setToolTipText("Integer");

        jTextField8.setToolTipText("Decimal");

        jTextField9.setToolTipText("Text");

        jLabel13.setFont(new java.awt.Font("Lucida Bright", 1, 13)); // NOI18N
        jLabel13.setText("Grade");

        jLabel14.setText("Response");

        jTextField10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField10ActionPerformed(evt);
            }
        });

        jButton2.setText("Reset");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Reset Grade");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(49, 49, 49)
                        .add(jLabel1))
                    .add(layout.createSequentialGroup()
                        .add(237, 237, 237)
                        .add(jLabel2))
                    .add(layout.createSequentialGroup()
                        .add(31, 31, 31)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(jLabel5)
                                .add(13, 13, 13)
                                .add(jTextField3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 86, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(54, 54, 54)
                                .add(jLabel9)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(jTextField6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE))
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(jRadioButton4)
                                            .add(jRadioButton3)
                                            .add(jRadioButton2))
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(layout.createSequentialGroup()
                                                .add(104, 104, 104)
                                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                    .add(jLabel8)
                                                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                                        .add(layout.createSequentialGroup()
                                                            .add(jButton1)
                                                            .add(50, 50, 50)
                                                            .add(jButton2)
                                                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 50, Short.MAX_VALUE)
                                                            .add(jButton3))
                                                        .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                                .add(jLabel3)
                                                                .add(jLabel4)
                                                                .add(jLabel6)
                                                                .add(jLabel11)
                                                                .add(jLabel10)
                                                                .add(jLabel12))
                                                            .add(31, 31, 31)
                                                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                                                .add(jTextField4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                                                                .add(jTextField2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                                                                .add(jTextField1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                                                                .add(jTextField7)
                                                                .add(jTextField8)
                                                                .add(jTextField9))))))
                                            .add(layout.createSequentialGroup()
                                                .add(167, 167, 167)
                                                .add(jLabel13))))
                                    .add(jRadioButton1))
                                .add(0, 0, Short.MAX_VALUE))
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabel14)
                                    .add(jLabel7))
                                .add(18, 18, 18)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jTextField5)
                                    .add(jTextField10))))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(15, 15, 15)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel3))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jTextField2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel4))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jTextField4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel6))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel13)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel10)
                            .add(jTextField7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel11)
                            .add(jTextField8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel12)
                            .add(jTextField9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(57, 57, 57)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jButton1)
                            .add(jButton2)
                            .add(jButton3))
                        .add(18, 18, 18)
                        .add(jLabel8))
                    .add(layout.createSequentialGroup()
                        .add(jRadioButton1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jRadioButton2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jRadioButton3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jRadioButton4)))
                .add(21, 21, 21)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(jTextField3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel9)
                    .add(jTextField6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(jTextField5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextField10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel14))
                .addContainerGap(87, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        LOG.info("Selecting radio button {}", jRadioButton1.getText());
        disableGradedItemId();
        if (getLinks().isEmpty()) {
            enableGradedItemCreate();
        } else if (linksContainsRelValue(RELATIONS_URI + SELF_REL_VALUE) && !isGradedItemSelf(jTextField1.getText())) {
            enableGradedItemCreate();
        } else if (linksContainsRelValue(RELATIONS_URI + GRADE_CREATE_VALUE)) {
            enableGradeCreate();
        }

    }//GEN-LAST:event_jRadioButton1ActionPerformed

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
        LOG.info("Selecting radio button {}", jRadioButton2.getText());
        if (getLinks().isEmpty() || (linksContainsRelValue(RELATIONS_URI + SELF_REL_VALUE) && !(linksContainsRelValue(RELATIONS_URI + GRADE_SELF_VALUE)))) {
            enableGradedItemRead();
        } else if (linksContainsRelValue(RELATIONS_URI + SELF_REL_VALUE) && (linksContainsRelValue(RELATIONS_URI + GRADE_SELF_VALUE))) {
            enableGradedItemRead();
            enableStudentId();

        }
    }//GEN-LAST:event_jRadioButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        LOG.info("Invoking REST Client based on selection");
        if (noActionSelected()) {
            JOptionPane.showMessageDialog(null, "Please choose an Action.", "Submit Error", JOptionPane.PLAIN_MESSAGE);
            return;
        }
        if (!isValidInput()) {
            return;
        }
        String gradedItemId;
        String studentId;
        ClientResponse clientResponse;

        if (jRadioButton1.isSelected()) {
            LOG.debug("Invoking {} action", jRadioButton1.getText());//Create
            LOG.debug("student Id = {}", jTextField7.getText());
            if (jTextField7.getText().trim().equals("")) {
                clientResponse = gradebook_HATEOAS_client.createGradedItem(this.convertFormToXMLString());
            } else {
                gradedItemId = jTextField1.getText();
                studentId = jTextField7.getText();
                clientResponse = gradebook_HATEOAS_client.createGrade(this.convertFormToXMLString(), studentId);
            }

            resourceURI = clientResponse.getLocation();
            LOG.debug("Retrieved location {}", resourceURI);

            this.populateForm(clientResponse);
        } else if (jRadioButton2.isSelected()) {
            LOG.debug("Invoking {} action", jRadioButton2.getText());// Read
            LOG.debug("student Id = {}", jTextField7.getText());
            gradedItemId = jTextField1.getText();
            if (jTextField7.getText().trim().equals("")) {
                disableDescription();
                if (linksContainsRelValue(RELATIONS_URI + SELF_REL_VALUE) && isGradedItemSelf(gradedItemId)) {
                    LOG.debug("Reading self");
                    clientResponse = gradebook_HATEOAS_client.retrieveGradedItem(ClientResponse.class);
                } else {
                    LOG.debug("Reading gradedItem by gradedItemId {}", gradedItemId);
                    clientResponse = gradebook_HATEOAS_client.retrieveGradedItem(ClientResponse.class, gradedItemId);
                }
            } else {
                studentId = jTextField7.getText();
                LOG.debug("calling read for student Id = {}", jTextField7.getText());
                if (isGradeSelf(studentId)) {
                    clientResponse = gradebook_HATEOAS_client.retrieveGrade(ClientResponse.class, studentId);
                } else {
                    LOG.debug("Reading student Id not self {}", jTextField1.getText());
                    JOptionPane.showMessageDialog(null, "Student Id not available.", "Submit Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            LOG.debug("clientResponse.getStatus() {}", clientResponse.getStatus());
            this.populateForm(clientResponse);
        } else if (jRadioButton3.isSelected()) {
            LOG.debug("Invoking {} action", jRadioButton3.getText());//Update
            LOG.debug("student Id = {}", jTextField7.getText());
            if (jTextField7.getText().trim().equals("")) {
                clientResponse = gradebook_HATEOAS_client.updateGradedItem(this.convertFormToXMLString());
            } else {
                clientResponse = gradebook_HATEOAS_client.updateGrade(this.convertFormToXMLString());
            }
            this.populateForm(clientResponse);
        } else if (jRadioButton4.isSelected()) {
            LOG.debug("Invoking {} action", jRadioButton4.getText());//Delete
            LOG.debug("student Id = {}", jTextField7.getText());
            gradedItemId = jTextField1.getText();
            if (jTextField7.getText().trim().equals("")) {
                if (isGradedItemSelf(gradedItemId)) {
                    clientResponse = gradebook_HATEOAS_client.deleteGradedItem();
                } else {
                    LOG.debug("Deleting gradedItem not in links gradedItemId {}", jTextField1.getText());
                    JOptionPane.showMessageDialog(null, "Graded Item Id not available.", "Submit Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } else {
                if (isGradeSelf(jTextField7.getText())) {
                    clientResponse = gradebook_HATEOAS_client.deleteGrade();
                } else {
                    LOG.debug("Deleting grade not self {}", jTextField7.getText());
                    JOptionPane.showMessageDialog(null, "Grade not available.", "Submit Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            this.populateForm(clientResponse);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jRadioButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton3ActionPerformed
        LOG.info("Selecting radio button {}", jRadioButton3.getText());
        if (linksContainsRelValue(RELATIONS_URI + SELF_REL_VALUE)) {
            enableGradedItemUpdate();
        } else if (linksContainsRelValue(RELATIONS_URI + GRADE_SELF_VALUE)) {
            enableGradeUpdate();
        }
    }//GEN-LAST:event_jRadioButton3ActionPerformed

    private void jRadioButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton4ActionPerformed
        LOG.info("Selecting radio button {}", jRadioButton4.getText());
        if (linksContainsRelValue(RELATIONS_URI + SELF_REL_VALUE)) {
            enableGradedItemDelete();
        } else if (linksContainsRelValue(RELATIONS_URI + GRADE_SELF_VALUE)) {
            enableGradeDelete();
        }
    }//GEN-LAST:event_jRadioButton4ActionPerformed

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void jTextField4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField4ActionPerformed

    private void jTextField3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField3ActionPerformed

    private void jTextField5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField5ActionPerformed

    private void jTextField6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField6ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField6ActionPerformed

    private void jTextField10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField10ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField10ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        reset();

    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        resetGrade();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void reset() {
        LOG.info("Resetting Display");
        //allow create and read to start
        clearLinks();
        disableAll();
        jRadioButton1.setEnabled(Boolean.TRUE);
        jRadioButton2.setEnabled(Boolean.TRUE);

        jTextField1.setText("");
        jTextField2.setText("");
        jTextField3.setText("");
        jTextField4.setText("");
        jTextField5.setText("");
        jTextField6.setText("");
        jTextField7.setText("");
        jTextField8.setText("");
        jTextField9.setText("");
        jTextField10.setText("");

    }

    private void resetGrade() {
        jTextField7.setText("");
        jTextField8.setText("");
        jTextField9.setText("");
        Link self = getSelfFromGradeSelf();
        clearLinks();
        addLink(self);

        ClientResponse clientResponse = gradebook_HATEOAS_client.retrieveGradedItem(ClientResponse.class);
        this.populateForm(clientResponse);
    }

    private void disableAll() {
        buttonGroup1.clearSelection();
        jRadioButton1.setEnabled(Boolean.FALSE);
        jRadioButton2.setEnabled(Boolean.FALSE);
        jRadioButton3.setEnabled(Boolean.FALSE);
        jRadioButton4.setEnabled(Boolean.FALSE);
        disableGradedItemId();
        disableDescription();
        disablePercentage();
        disableStudentId();
        disableScore();
        disableComment();
    }

    private void enableGradedItemCreate() {
        LOG.info("enableGradedItemRead");
        jRadioButton1.setEnabled(Boolean.TRUE);
        disableGradedItemId();
        enablePercentage();
        enableDescription();
        disableStudentId();
        disableScore();
        disableComment();
        jTextField7.setText("");
        jTextField8.setText("");
        jTextField9.setText("");
    }

    private void enableGradedItemRead() {
        LOG.debug("enableGradedItemRead");
        jRadioButton2.setEnabled(Boolean.TRUE);
        enableGradedItemId();
        disableDescription();
        disablePercentage();
        disableStudentId();
        disableScore();
        disableComment();
        jTextField7.setText("");
        jTextField8.setText("");
        jTextField9.setText("");
    }

    private void disableGradedItemRead() {
        LOG.debug("disableGradedItemRead");
        jRadioButton2.setEnabled(Boolean.FALSE);
        disableGradedItemId();

    }

    private void enableGradedItemUpdate() {
        LOG.debug("enableGradedItemUpdate");
        jRadioButton3.setEnabled(Boolean.TRUE);
        disableGradedItemId();
        enableDescription();
        enablePercentage();
        disableStudentId();
        disableScore();
        disableComment();

    }

    private void enableGradedItemDelete() {
        LOG.debug("enableGradedItemDelete");
        jRadioButton4.setEnabled(Boolean.TRUE);
        enableGradedItemId();
        disableDescription();
        disablePercentage();
        disableStudentId();
        disableScore();
        disableComment();

    }

    private void disableGradedItemId() {
        LOG.debug("disableGradedItemId");
        jLabel3.setEnabled(Boolean.FALSE);
        jTextField1.setEnabled(Boolean.FALSE);
    }

    private void enableGradedItemId() {
        LOG.debug("enableGradedItemId");
        jLabel3.setEnabled(Boolean.TRUE);
        jTextField1.setEnabled(Boolean.TRUE);
    }

    private void disableDescription() {
        LOG.debug("disableDescription");
        jLabel4.setEnabled(Boolean.FALSE);
        jTextField2.setEnabled(Boolean.FALSE);
    }

    private void enableDescription() {
        LOG.debug("enableDescription");
        jLabel4.setEnabled(Boolean.TRUE);
        jTextField2.setEnabled(Boolean.TRUE);
    }

    private void disablePercentage() {
        LOG.debug("disablePercentage");
        jLabel6.setEnabled(Boolean.FALSE);
        jTextField4.setEnabled(Boolean.FALSE);
    }

    private void enablePercentage() {
        LOG.debug("enablePercentage");
        jLabel6.setEnabled(Boolean.TRUE);
        jTextField4.setEnabled(Boolean.TRUE);
    }

    private void enableGradeRead() {
        LOG.debug("enableGradeRead");
        jRadioButton2.setEnabled(Boolean.TRUE);
        disableGradedItemId();
        disableDescription();
        disablePercentage();
        enableStudentId();
        disableScore();
        disableComment();
    }

    private void enableGradeUpdate() {
        LOG.debug("enableGradeUpdate");
        jRadioButton3.setEnabled(Boolean.TRUE);
        disableGradedItemId();
        disableDescription();
        disablePercentage();
        disableStudentId();
        enableScore();
        enableComment();
    }

    private void enableGradeDelete() {
        LOG.debug("enableGradeDelete");
        jRadioButton4.setEnabled(Boolean.TRUE);
        disableGradedItemId();
        disableDescription();
        disablePercentage();
        enableStudentId();
        disableScore();
        disableComment();
    }

    private void disableGradeCreate() {
        LOG.debug("disableGradeCreate");
        disableStudentId();
        disableScore();
        disableComment();
    }

    private void enableGradeCreate() {
        LOG.debug("enableGradeCreate");
        jRadioButton1.setEnabled(Boolean.TRUE);
        disableGradedItemId();
        disableDescription();
        disablePercentage();
        enableStudentId();
        enableScore();
        enableComment();
    }

    private void disableStudentId() {
        LOG.debug("disableStudentId");
        jLabel10.setEnabled(Boolean.FALSE);
        jTextField7.setEnabled(Boolean.FALSE);
    }

    private void disableStudentIdText() {
        LOG.debug("disableStudentIdText");
        jLabel10.setEnabled(Boolean.TRUE);
        jTextField7.setEnabled(Boolean.FALSE);
    }

    private void enableStudentId() {
        LOG.debug("enableStudentId");
        jLabel10.setEnabled(Boolean.TRUE);
        jTextField7.setEnabled(Boolean.TRUE);
    }

    private void disableScore() {
        LOG.debug("disableStudentScore");
        jLabel11.setEnabled(Boolean.FALSE);
        jTextField8.setEnabled(Boolean.FALSE);
    }

    private void enableScore() {
        LOG.debug("enableScore");
        jLabel11.setEnabled(Boolean.TRUE);
        jTextField8.setEnabled(Boolean.TRUE);
    }

    private void disableComment() {
        LOG.debug("disableStudentComment");
        jLabel12.setEnabled(Boolean.FALSE);
        jTextField9.setEnabled(Boolean.FALSE);
    }

    private void enableComment() {
        LOG.debug("enableComment");
        jLabel12.setEnabled(Boolean.TRUE);
        jTextField9.setEnabled(Boolean.TRUE);
    }

    private boolean isValidInput() {
        boolean isValid = true;
        if ((!jTextField1.getText().trim().equals("")) && (!isInteger(jTextField1.getText()))) {
            LOG.debug("Invalid gradedItemId {}", jTextField1.getText());
            JOptionPane.showMessageDialog(null, "Graded Item Id must be an integer.", "Submit Error", JOptionPane.WARNING_MESSAGE);
            isValid = false;
        } else if ((!jTextField7.getText().trim().equals("")) && (!isInteger(jTextField7.getText()))) {
            LOG.debug("Invalid studentId {}", jTextField7.getText());
            JOptionPane.showMessageDialog(null, "Student Id must be an integer.", "Submit Error", JOptionPane.WARNING_MESSAGE);
            isValid = false;
        } else if ((!jTextField4.getText().trim().equals("")) && (!isFloat(jTextField4.getText()))) {
            LOG.debug("Invalid percentage {}", jTextField4.getText());
            JOptionPane.showMessageDialog(null, "Graded Item percentage must be a decimal type.", "Submit Error", JOptionPane.WARNING_MESSAGE);
            isValid = false;
        } else if ((!jTextField8.getText().trim().equals("")) && (!isFloat(jTextField8.getText()))) {
            LOG.debug("Invalid score {}", jTextField8.getText());
            JOptionPane.showMessageDialog(null, "Student score must be a decimal type.", "Submit Error", JOptionPane.WARNING_MESSAGE);
            isValid = false;
        }
        return isValid;
    }

    private boolean isInteger(String s) {
        return isInteger(s, 10);
    }

    private boolean isInteger(String s, int radix) {
        if (s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (i == 0 && s.charAt(i) == '-') {
                if (s.length() == 1) {
                    return false;
                } else {
                    continue;
                }
            }
            if (Character.digit(s.charAt(i), radix) < 0) {
                return false;
            }
        }
        return true;
    }

    private boolean noActionSelected() {
        return !(jRadioButton1.isSelected()
                || jRadioButton2.isSelected()
                || jRadioButton3.isSelected()
                || jRadioButton4.isSelected());
    }

    private boolean isFloat(String s) {
        Pattern DOUBLE_PATTERN = Pattern.compile(
                "[\\x00-\\x20]*[+-]?(NaN|Infinity|((((\\p{Digit}+)(\\.)?((\\p{Digit}+)?)"
                + "([eE][+-]?(\\p{Digit}+))?)|(\\.((\\p{Digit}+))([eE][+-]?(\\p{Digit}+))?)|"
                + "(((0[xX](\\p{XDigit}+)(\\.)?)|(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+)))"
                + "[pP][+-]?(\\p{Digit}+)))[fFdD]?))[\\x00-\\x20]*");

        return DOUBLE_PATTERN.matcher(s).matches();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Gradebook_HATEOAS_UI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Gradebook_HATEOAS_UI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Gradebook_HATEOAS_UI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Gradebook_HATEOAS_UI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Gradebook_HATEOAS_UI().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField10;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    // End of variables declaration//GEN-END:variables

}
