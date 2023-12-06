
import sys
import control.EmailService as EmailService
import control.parameters as parameters
import threading
import time
import random
import math
import MySQLdb
import os


# USER[USER_ID] = CORDINATE
# USER_ETA[USER_ID] = DATETIME
# USER_OTP[USER_ID] = OTP
# SESSION_ID[ID] = USER_ID
# PARTY_LOCATION[PARTY_ID] = [userid: coordinate, …]
# SESSION_ID[ID] = [List Of Messages]


class AccountManager():
    def __init__(self):
        self.connection = MySQLdb.connect(
            host=parameters.SERVER_NAME,
            user=parameters.MYSQL_USERNAME,
            passwd=parameters.MYSQL_PASSWORD,
            db=parameters.MYSQL_DB,
        )
        self.connection.ping(True)
        self.userCoordinatesAndETA = {}
        self.userOTP = {}
        self.sessionID = {}
        self.messages = {}
        self.keepDBAliveThread = threading.Thread(target=self.keepDBAlive)
        # self.keepDBAliveThread.start()
        self.emailService = EmailService.EmailService()

    def keepDBAlive(self):
        while True:
            try:
                self.checkMobileExist(1, ifPrint=False)
            except Exception as e:
                self.connection = MySQLdb.connect(
                    host=parameters.SERVER_NAME,
                    user=parameters.MYSQL_USERNAME,
                    passwd=parameters.MYSQL_PASSWORD,
                    db=parameters.MYSQL_DB,
                )
                print("Reconnect!", e)
            time.sleep(1)

    def generateOTP(self):
        digits = "0123456789"
        OTP = ""
        for i in range(6):
            OTP += digits[math.floor(random.random() * 10)]
        return OTP

    def validateSessionID(self, session):
        if str(session) in self.sessionID:
            return True, self.sessionID[str(session)][0], self.sessionID[session][1]
        else:
            return False, 0, ""
    
    def sendNotification(self, USERID, message):
        USERID = str(USERID)
        if USERID in self.messages:
            self.messages[USERID].append(message)
        else:
            self.messages[USERID] = []
            self.messages[USERID].append(message)
    
    def getNotification(self, USERID):
        USERID = str(USERID)
        if USERID in self.messages:
            res = self.messages[USERID].copy()
            self.messages[USERID] = []
            print("got", res, " for user ", USERID)
            return True, res
        else:
            return False, []

    def addNewUser(self, USERNAME, PASSWORD_HASH, EMAIL, PHONENUMBER):
        if self.checkEmailExist(EMAIL):
            return False, "Email Exist!"
        if self.checkMobileExist(PHONENUMBER):
            return False, "Phone Exist!"
        cursor = self.connection.cursor()
        sql = "INSERT INTO USERS (USERNAME, PASSWORD_HASH, EMAIL, PHONENUMBER) VALUES (%s, %s, %s, %s)"
        val = (USERNAME, PASSWORD_HASH, EMAIL, PHONENUMBER)
        result = cursor.execute(sql, val)
        self.connection.commit()
        return True, "OK"

    def getOTPMobile(self, mobile, register=False):
        if (register and (not self.checkMobileExist(mobile))) or ((not register) and self.checkMobileExist(mobile)):
            otp = self.generateOTP()
            self.userOTP[mobile] = otp
            print("GEN OTP: ", mobile, ": ", otp)
            return True, otp
        else:
            return False, ""

    def getOTPEmail(self, email, register=False):
        if (register and (not self.checkEmailExist(email))) or ((not register) and self.checkEmailExist(email)):
            otp = self.generateOTP()
            self.userOTP[email] = otp
            self.emailService.sendOTP(email, otp)
            print("GEN OTP: ", email, ": ", otp)
            return True, otp
        else:
            return False, ""

    def validateOTPMobile(self, mobile, otp):
        if mobile not in self.userOTP:
            return False, "Request OTP First!"
        else:
            if self.userOTP[mobile] == otp:
                return True, "OK"
            else:
                return False, "Wrong OTP!"

    def validateOTPEmail(self, email, otp):
        if email not in self.userOTP:
            return False, "Request OTP First!"
        else:
            if self.userOTP[email] == otp:
                return True,  "OK"
            else:
                return False, "Wrong OTP!"

    def checkEmailExist(self, email):
        email = str(email)
        cursor = self.connection.cursor()
        sql = "SELECT EMAIL FROM USERS WHERE EMAIL = \"" + email + "\""
        print(sql)
        result = cursor.execute(sql)
        print("check email", email, " exist result: ", result)
        if result != 0:
            print("return true")
            return True
        else:
            return False

    def checkMobileExist(self, mobile, ifPrint=True):
        mobile = str(mobile)
        cursor = self.connection.cursor()
        sql = "SELECT PHONENUMBER FROM USERS WHERE PHONENUMBER = \"" + \
            str(mobile) + "\""
        if ifPrint:
            print(sql)
        result = cursor.execute(sql)
        if ifPrint:
            print("check phone", mobile, " exist result: ", result)
        if result != 0:
            return True
        else:
            return False

    def updateEmailPassword(self, email, newPassword):
        if self.checkEmailExist(email):
            cursor = self.connection.cursor()
            sql = "UPDATE USERS SET PASSWORD_HASH = " + \
                "\"" + newPassword + "\"" + " WHERE EMAIL = \"" + email + "\""
            print(sql)
            result = cursor.execute(sql)
            self.connection.commit()
            print("Update password:", result)
            return True, "OK!"
        else:
            return False, "Email Doesn't Exist!"

    def updatePhonePassword(self, phone, newPassword):
        if self.checkMobileExist(phone):
            cursor = self.connection.cursor()
            sql = "UPDATE USERS SET PASSWORD_HASH = " + \
                "\"" + newPassword + "\"" + " WHERE PHONENUMBER = \"" + phone + "\""
            print(sql)
            result = cursor.execute(sql)
            self.connection.commit()
            print("Update password:", result)
            return True, "OK!"
        else:
            return False, "Phone Doesn't Exist!"

    def genSessionID(self, userID, userName):
        sessionIDThis = random.randrange(1000000000, 9999999999)
        while sessionIDThis in self.sessionID:
            sessionIDThis = random.randrange(1000000000, 9999999999)
        self.sessionID[str(sessionIDThis)] = (userID, userName)
        return sessionIDThis

    def externalLoginEmail(self, email):
        if self.checkEmailExist(email):
            sql = "SELECT USER_ID, USERNAME, PASSWORD_HASH FROM USERS WHERE EMAIL = \"" + email + "\""
            print(sql)
            cursor = self.connection.cursor()
            result = cursor.execute(sql)
            if result > 0:
                m = cursor.fetchall()[0]
                res = m
                userID = res[0]
                userName = res[1]
                sessionIDThis = self.genSessionID(userID, userName)
                # self.sessionID[sessionIDThis] = userID
                return True, sessionIDThis, userID,  "Login success!"
            else:
                return False, 0, 0, "UNKOWN ERR externalLoginEmail!"

        else:
            return False, 0, 0, "User Not Exist!"

    def externalLoginMobile(self, mobile):
        if self.checkMobileExist(mobile):
            sql = "SELECT USER_ID, USERNAME, PASSWORD_HASH FROM USERS WHERE PHONENUMBER = \"" + \
                str(mobile) + "\""
            print(sql)
            cursor = self.connection.cursor()
            result = cursor.execute(sql)
            if result > 0:
                m = cursor.fetchall()[0]
                res = m
                userID = res[0]
                userName = res[1]
                sessionIDThis = self.genSessionID(userID, userName)
                # self.sessionID[sessionIDThis] = userID
                return True, sessionIDThis, userID,  "Login success!"
            else:
                return False, 0, 0, "UNKOWN ERR externalLoginEmail!"

        else:
            return False, 0, 0, "User Not Exist!"

    def validateEmailPassword(self, email, password):
        if self.checkEmailExist(email):
            sql = "SELECT USER_ID, USERNAME, PASSWORD_HASH FROM USERS WHERE EMAIL = \"" + email + "\""
            print(sql)
            cursor = self.connection.cursor()
            result = cursor.execute(sql)
            if result > 0:
                m = cursor.fetchall()[0]
                res = m
                userID = res[0]
                userName = res[1]
                if res[2] == password:
                    sessionIDThis = self.genSessionID(userID, userName)
                    # self.sessionID[sessionIDThis] = userID
                    return True, sessionIDThis, userID,  "Login success!"
                else:
                    return False, 0, userID,  "Wrong Password!"
            else:
                return False, 0, 0, "UNKOWN ERR validateEmailPassword!"

        else:
            return False, 0, 0, "User Not Exist!"

    def validatePhonePassword(self, phone, password):
        if self.checkMobileExist(phone):
            sql = "SELECT USER_ID, USERNAME, PASSWORD_HASH FROM USERS WHERE PHONENUMBER = \"" + phone + "\""
            print(sql)
            cursor = self.connection.cursor()
            result = cursor.execute(sql)
            if result > 0:
                m = cursor.fetchall()[0]
                res = m
                userID = res[0]
                userName = res[1]
                if res[2] == password:
                    sessionIDThis = self.genSessionID(userID, userName)
                    # self.sessionID[sessionIDThis] = userID
                    return True, sessionIDThis, userID,  "Login success!"
                else:
                    return False, 0, userID,  "Wrong Password!"
            else:
                return False, 0, 0, "UNKOWN ERR validateEmailPassword!"

        else:
            return False, 0, 0, "User Not Exist!"

    def validatePhoneOTP(self, phone, OTP):
        if self.checkMobileExist(phone):
            sql = "SELECT USER_ID, USERNAME FROM USERS WHERE PHONENUMBER = \"" + phone + "\""
            print(sql)
            cursor = self.connection.cursor()
            result = cursor.execute(sql)
            if result > 0:
                m = cursor.fetchall()[0]
                res = m
                userID = res[0]
                userName = res[1]
                if phone in self.userOTP:
                    if self.userOTP[phone] == OTP:
                        sessionIDThis = self.genSessionID(userID, userName)
                        # self.sessionID[sessionIDThis] = userID
                        # del self.sessionID[sessionIDThis]
                        del self.userOTP[phone]
                        return True, sessionIDThis, userID,  "Login success!"
                    else:
                        return False, 0, userID,  "Wrong OTP!"
                else:
                    return False, 0, userID,  "Please First Get Your OTP!"
            else:
                return False, 0, 0, "UNKOWN ERR validatePhoneOTP!"
        else:
            return False, 0, 0, "User Not Exist!"

    def validateEmailOTP(self, email, OTP):
        if self.checkEmailExist(email):
            sql = "SELECT USER_ID, USERNAME FROM USERS WHERE EMAIL = \"" + email + "\""
            print(sql)
            cursor = self.connection.cursor()
            result = cursor.execute(sql)
            if result > 0:
                m = cursor.fetchall()[0]
                res = m
                userID = res[0]
                userName = res[1]
                if email in self.userOTP:
                    if self.userOTP[email] == OTP:
                        sessionIDThis = self.genSessionID(userID, userName)
                        # self.sessionID[sessionIDThis] = userID
                        # del self.sessionID[sessionIDThis]
                        del self.userOTP[email]
                        return True, sessionIDThis, userID,  "Login success!"
                    else:
                        return False, 0, userID,  "Wrong OTP!"
                else:
                    return False, 0, userID,  "Please First Get Your OTP!"
            else:
                return False, 0, 0, "UNKOWN ERR validateEmailOTP!"
        else:
            return False, 0, 0, "User Not Exist!"

    # if __name__ == "__main__":
    #     cursor = self.connection.cursor()
    #     cursor.execute("select @@version")
    #     version = cursor.fetchall()[0]

    #     print("96259760:", self.checkMobileExist(96259760))
    #     # updateEmailPassword("fty20020322@gmail.com", "12345")

    #     if version:
    #         print('Running version: ', version)
    #     else:
    #         print('Not connected.')

    #     addNewUser("eric", "eric", "fty20020322@gmail.com", "96259760")
    #     print(validateEmailPassword("fty20020322@gmail.com", "12345"))
