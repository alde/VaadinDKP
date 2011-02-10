/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity;

import com.google.gson.Gson;
import com.unknown.entity.dao.RaidDAO;
import com.unknown.entity.database.RaidDB;
import com.unknown.entity.raids.Raid;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author alde
 */
public class GetRaidsJSON extends HttpServlet {

        /**
         * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
         * @param request servlet request
         * @param response servlet response
         * @throws ServletException if a servlet-specific error occurs
         * @throws IOException if an I/O error occurs
         */
        protected void processRequest(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
                response.setContentType("text/html;charset=UTF-8");
                PrintWriter out = response.getWriter();
                try {
                        RaidDAO raidDao = new RaidDB();
                        List<Raid> raids = raidDao.getRaids();
                        for (Raid r : raids) {
                                try {
                                        r.setRaidRewards(raidDao.getRewardsForRaid(r.getId()));
                                        r.setRaidItems(raidDao.getItemsForRaid(r.getId()));

                                } catch (SQLException ex) {
                                        Logger.getLogger(GetRaidsJSON.class.getName()).log(Level.SEVERE, null, ex);
                                }
                        }
                        Gson gson = new Gson();
                        String userJson = gson.toJson(raids);
                        System.out.println(userJson);
                        out.println(userJson);
                } finally {
                        out.close();
                }
        }

        // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
        /**
         * Handles the HTTP <code>GET</code> method.
         * @param request servlet request
         * @param response servlet response
         * @throws ServletException if a servlet-specific error occurs
         * @throws IOException if an I/O error occurs
         */
        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
                processRequest(request, response);
        }

        /**
         * Handles the HTTP <code>POST</code> method.
         * @param request servlet request
         * @param response servlet response
         * @throws ServletException if a servlet-specific error occurs
         * @throws IOException if an I/O error occurs
         */
        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
                processRequest(request, response);
        }

        /**
         * Returns a short description of the servlet.
         * @return a String containing servlet description
         */
        @Override
        public String getServletInfo() {
                return "Short description";
        }// </editor-fold>
}
