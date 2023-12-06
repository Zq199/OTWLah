import os
import MySQLdb
import control.parameters as parameters
import math
import random
import time
import threading

# USER[USER_ID] = CORDINATE
# USER_ETA[USER_ID] = DATETIME
# USER_OTP[USER_ID] = OTP
# SESSION_ID[ID] = USER_ID
# PARTY_LOCATION[PARTY_ID] = {userid: coordinate, …}
# STA: 0: Active 1: Finished


class PartyManager():
    def __init__(self):
        self.partyLocation = {}
        self.connection = MySQLdb.connect(
            host=parameters.SERVER_NAME,
            user=parameters.MYSQL_USERNAME,
            passwd=parameters.MYSQL_PASSWORD,
            db=parameters.MYSQL_DB,
        )
        self.connection.ping(True)
        sql = "SELECT PARTY_ID, STA FROM PARTY"
        print(sql)
        cursor = self.connection.cursor()
        result = cursor.execute(sql)
        res = cursor.fetchall()
        for oneparty in res:
            partyID = oneparty[0]
            sta = oneparty[1]
            if str(sta) == "0":
                print("Party ID", partyID, "added party locations.")
                self.partyLocation[str(partyID)] = {}
        self.keepDBAliveThread = threading.Thread(target=self.keepDBAlive)
        self.keepDBAliveThread.start()

    def keepDBAlive(self):
        while True:
            try:
                self.checkPartyExistNPrint(1)
            except Exception as e:
                self.connection = MySQLdb.connect(
                    host=parameters.SERVER_NAME,
                    user=parameters.MYSQL_USERNAME,
                    passwd=parameters.MYSQL_PASSWORD,
                    db=parameters.MYSQL_DB,
                )
                print("Reconnect Party!", e)
            time.sleep(1)

    def createNewParty(self, PARTY_LEADER, DESTINATION, DESTINATION_COORDINATE, DESTINATION_LOCATION, PREFERRED_ARRIVAL_TIME):
        cursor = self.connection.cursor()
        sql = "INSERT INTO PARTY (PARTY_LEADER, DESTINATION, DESTINATION_COORDINATE, DESTINATION_LOCATION, PREFERRED_ARRIVAL_TIME, STA) VALUES (%s, %s, %s, %s, %s, %s)"
        val = (PARTY_LEADER, DESTINATION,
               DESTINATION_COORDINATE, DESTINATION_LOCATION, PREFERRED_ARRIVAL_TIME, 0)
        result = cursor.execute(sql, val)
        self.connection.commit()
        if result == 1:
            sql = "SELECT MAX(PARTY_ID) FROM PARTY WHERE PREFERRED_ARRIVAL_TIME = \"" + \
                PREFERRED_ARRIVAL_TIME + "\" AND DESTINATION = \"" + DESTINATION + "\""
            print(sql)
            result = cursor.execute(sql)
            if result > 0:
                m = cursor.fetchall()[0]
                res = m
                partyID = res[0]
                self.joinParty(partyID, PARTY_LEADER)
                self.partyLocation[str(partyID)] = {}
                return True, partyID, "OK!"
            else:
                return False, 0, "Party Create Failed!"
        else:
            return False, 0, "FAILED"

    def checkPartyExistNPrint(self, PARTYID, ifprint=True):
        sql = "SELECT PARTY_ID FROM PARTY WHERE PARTY_ID = " + str(PARTYID)
        cursor = self.connection.cursor()
        result = cursor.execute(sql)
        if result > 0:
            m = cursor.fetchall()[0]
            res = m
            partyID = res[0]
            return True
        else:
            return False

    def checkPartyExist(self, PARTYID, ifprint=True):
        sql = "SELECT PARTY_ID FROM PARTY WHERE PARTY_ID = " + str(PARTYID)
        if ifprint:
            print(sql)
        cursor = self.connection.cursor()
        result = cursor.execute(sql)
        if result > 0:
            m = cursor.fetchall()[0]
            res = m
            partyID = res[0]
            return True
        else:
            return False

    def checkPartyLeader(self, PARTYID):
        if self.checkPartyExist(PARTYID):
            sql = "SELECT PARTY_LEADER FROM PARTY WHERE PARTY_ID = " + \
                str(PARTYID)
            print(sql)
            cursor = self.connection.cursor()
            result = cursor.execute(sql)
            if result > 0:
                m = cursor.fetchall()[0]
                res = m
                leader = res[0]
                return True, leader, "OK"
            else:
                return False, "", "Internal Error! checkPartyLeader"
        else:
            return False, "", "Party Doens't Exist!"

    def checkUserPartyID(self, USERID):
        sql = "SELECT PARTY_ID FROM JOINED_PARTY WHERE USER_ID = " + \
            str(USERID)
        print(sql)
        cursor = self.connection.cursor()
        result = cursor.execute(sql)
        if result > 0:
            m = cursor.fetchall()[0]
            res = m
            partyID = res[0]
            return True, partyID, "OK"
        else:
            return False, 0, "No Party Found Assosiacted with User!"

    def checkPartyExist(self, PARTYID):
        sql = "SELECT PARTY_ID FROM PARTY WHERE PARTY_ID = " + str(PARTYID)
        print(sql)
        cursor = self.connection.cursor()
        result = cursor.execute(sql)
        if result > 0:
            m = cursor.fetchall()[0]
            res = m
            partyID = res[0]
            return True
        else:
            return False

    def updateParty(self, PARTYID, PARTY_LEADER, DESTINATION, DESTINATION_COORDINATE, DESTINATION_LOCATION, PREFERRED_ARRIVAL_TIME):
        PARTYID = str(PARTYID)
        if self.checkPartyExist(PARTYID):
            cursor = self.connection.cursor()
            sql = "UPDATE PARTY SET " + \
                "PARTY_LEADER = " + str(PARTY_LEADER) + ", " + \
                "DESTINATION_COORDINATE = \"" + str(DESTINATION_COORDINATE) + "\", " + \
                "DESTINATION = \"" + DESTINATION + "\", " + \
                "DESTINATION_LOCATION = \"" + DESTINATION_LOCATION + "\", " + \
                "PREFERRED_ARRIVAL_TIME = \"" + \
                str(PREFERRED_ARRIVAL_TIME) + \
                "\" WHERE PARTY_ID = " + str(PARTYID)
            print(sql)
            result = cursor.execute(sql)
            self.connection.commit()
            if result == 1:
                return True, PARTYID, "OK!"
            else:
                return False, 0, "FAILED"
        else:
            return False, 0, "PARTY NOT FOUND"

    def checkUserInParty(self, USERID):
        sql = "SELECT PARTY_ID FROM JOINED_PARTY WHERE USER_ID = " + \
            str(USERID)
        print(sql)
        cursor = self.connection.cursor()
        result = cursor.execute(sql)
        if result > 0:
            m = cursor.fetchall()[0]
            res = m
            partyID = res[0]
            return True, partyID
        else:
            return False, []
    
    def checkPartyMemberCnt(self, PARTYID):
        sql = "SELECT * FROM PARTY_HISTORY WHERE PARTY_ID = " + \
            str(PARTYID)
        print(sql)
        cursor = self.connection.cursor()
        result = cursor.execute(sql)
        if result > 0:
            m = cursor.fetchall()
            return True, result
        else:
            return False, 0
    
    # def checkPartyLeader(self, PARTYID):
    #     sql = "SELECT PARTY_LEADER FROM PARTY WHERE PARTY_ID = " + \
    #         str(PARTYID)
    #     print(sql)
    #     cursor = self.connection.cursor()
    #     result = cursor.execute(sql)
    #     if result > 0:
    #         m = cursor.fetchall()[0]
    #         res = m
    #         partyID = res[0]
    #         return True, partyID
    #     else:
    #         return False, []
    
    def checkUserInPartyHistory(self, USERID):
        sql = "SELECT PARTY_ID FROM PARTY_HISTORY WHERE USER_ID = " + \
            str(USERID)
        print(sql)
        cursor = self.connection.cursor()
        result = cursor.execute(sql)
        results = []
        if result > 0:
            sql = "SELECT PARTY_ID, PARTY_LEADER, DESTINATION, DESTINATION_COORDINATE, DESTINATION_LOCATION, PREFERRED_ARRIVAL_TIME, STA from PARTY WHERE PARTY_ID IN ("
            m = cursor.fetchall()
            for i in m:
                id = i[0]
                sql += str(id)
                sql += ","
            sql = sql[:-1]
            sql += ")"
            print(sql)
            cursor = self.connection.cursor()
            result = cursor.execute(sql)
            if result > 0:
                m = cursor.fetchall()
                for i in m:
                    # PARTY_ID, PARTY_LEADER, DESTINATION, DESTINATION_COORDINATE, PREFERRED_ARRIVAL_TIME, STA
                    results.append({"PARTY_ID": str(i[0]), "PARTY_LEADER": str(i[1]), "DESTINATION": str(i[2]), "DESTINATION_COORDINATE": str(i[3]), "DESTINATION_LOCATION": str(i[4]), "PREFERRED_ARRIVAL_TIME": str(i[5]), "STA" : str(i[6])})
        return True, results

    def quitParty(self, USERID):
        USERID = str(USERID)
        sql = "DELETE FROM JOINED_PARTY WHERE USER_ID = " + str(USERID)
        print(sql)
        print("REMOVED FROM PARTY")
        cursor = self.connection.cursor()
        result = cursor.execute(sql)
        self.connection.commit()
        if result > 0:
            return True
        else:
            return False
    
    def quitPartyCompletely(self, USERID, PARTYID):

        sta, partyLeader, msg = self.checkPartyLeader(PARTYID)
        sta1, cnt = self.checkPartyMemberCnt(PARTYID)

        sql = "DELETE FROM JOINED_PARTY WHERE PARTY_ID = " + str(PARTYID) + " AND USER_ID = " + str(USERID)
        print(sql)
        cursor = self.connection.cursor()
        result = cursor.execute(sql)
        self.connection.commit()

        sql = "DELETE FROM PARTY_HISTORY WHERE PARTY_ID = " + str(PARTYID) + " AND USER_ID = " + str(USERID)
        print(sql)
        cursor = self.connection.cursor()
        result = cursor.execute(sql)
        self.connection.commit()

        if sta and str(partyLeader) == str(USERID):
            sql = "UPDATE PARTY SET PARTY_LEADER = (SELECT MIN(USER_ID) FROM PARTY_HISTORY WHERE PARTY_ID = " + str(PARTYID) + ") WHERE PARTY_ID = "  + str(PARTYID) + ";"
            try:
                print("Party member: ", cnt)
                print(sql)
                cursor = self.connection.cursor()
                result = cursor.execute(sql)
                self.connection.commit()
            except:
                print("No on in this party anymore!")
        
        if result > 0:
            return True
        else:
            return False

    def joinParty(self, PARTYID, USERID):
        PARTYID = str(PARTYID)
        USERID = str(USERID)
        if self.checkPartyExist(PARTYID):
            userInParty, partyID = self.checkUserInParty(PARTYID)
            self.quitParty(USERID)
            cursor = self.connection.cursor()
            sql = "INSERT INTO PARTY_HISTORY (PARTY_ID, USER_ID) VALUES (%s, %s)"
            val = (PARTYID, USERID)
            print(sql)
            result = cursor.execute(sql, val)
            self.connection.commit()
            self.quitParty(USERID)
            cursor = self.connection.cursor()
            sql = "INSERT INTO JOINED_PARTY (PARTY_ID, USER_ID) VALUES (%s, %s)"
            val = (PARTYID, USERID)
            print(sql)
            result = cursor.execute(sql, val)
            self.connection.commit()
            if result == 1:
                return True, PARTYID, "OK!"
            else:
                return False, 0, "Party John Failed!"
        else:
            return False, 0, "Party Not Found!"

    def getPartyStatus(self, PARTYID):
        PARTYID = str(PARTYID)
        if self.checkPartyExist(PARTYID):
            if PARTYID in self.partyLocation:
                return True, PARTYID, self.partyLocation[PARTYID]
            else:
                return False, 0, {}
        else:
            return False, 0, {}

    def updatePartyStatus(self, PARTYID, USERID, USERNAME, USERLOCATIONCOORDINATE, ETA):
        PARTYID = str(PARTYID)
        USERID = str(USERID)
        if self.checkPartyExist(PARTYID):
            if PARTYID in self.partyLocation:
                self.partyLocation[PARTYID][USERID] = (
                    USERNAME, USERLOCATIONCOORDINATE, ETA)
                return True, PARTYID, "OK"
            else:
                self.partyLocation[PARTYID] = {}
                self.partyLocation[PARTYID][USERID] = (
                    USERNAME, USERLOCATIONCOORDINATE, ETA)
                return True, PARTYID, "OK"
        else:
            return False, 0, "Party Not Found!"

    def getPartyDetails(self, PARTYID):
        PARTYID = str(PARTYID)
        if self.checkPartyExist(PARTYID):
            sql = "SELECT PARTY_ID, PARTY_LEADER, DESTINATION, DESTINATION_COORDINATE, DESTINATION_LOCATION, PREFERRED_ARRIVAL_TIME, STA FROM PARTY WHERE PARTY_ID = " + \
                str(PARTYID)
            print(sql)
            cursor = self.connection.cursor()
            result = cursor.execute(sql)
            if result > 0:
                m = cursor.fetchall()[0]
                res = m
                return True, res, "Party Found!"
            else:
                return False, (), "Internal Error!"
        else:
            return False, (), "Party Not Found!"

    # if __name__ == "__main__":
    #     cursor = connection.cursor()
    #     cursor.execute("select @@version")
    #     version = cursor.fetchall()[0]
    #     testparty = createNewParty(1, "NTU", "NTU", "2023-10-11 23:59:00")[1]
    #     print(testparty)
    #     print(joinParty(1, 1))
    #     print(updateParty(testparty, 1, "NTU", "NTU2", "2023-10-11 23:59:00"))
