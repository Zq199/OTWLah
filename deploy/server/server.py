from flask import Flask, request, Response
import control.parameters as parameters
import control.account as account
import control.party as party
import control.EmailService as EmailService
import json


def checkArgsExist(args, checkList):
    for arg in checkList:
        if args.get(arg) == None:
            return False
    return True


accountManager = account.AccountManager()
partyManager = party.PartyManager()
emailService = EmailService.EmailService()

app = Flask(__name__)


@app.route('/', methods=['GET'])
def root():
    return json.dumps({"status": "ok"})


@app.route('/otps', methods=['GET'])
def debugOTP():
    return accountManager.userOTP


@app.route('/sessions', methods=['GET'])
def debugSessions():
    return accountManager.sessionID

@app.route('/sessionsinfo', methods=['GET'])
def getSessionInfo():
    args = request.args
    if checkArgsExist(args, ["session"]):
        session = args.get("session")
        sessionSta, userID, userName = accountManager.validateSessionID(
            session)
        if sessionSta:
            return json.dumps({"status": "ok", "message": "ok", "username": userName, "userID": userID})
        else:
            return json.dumps({"status": "error", "message": "Session Invalid!"})
    return json.dumps({"status": "error", "message": "invalid args"})

@app.route('/user/notification/send', methods=['GET'])
def sendNotification():
    args = request.args
    if checkArgsExist(args, ["session", "userid", "notification"]):
        session = args.get("session")
        userid = args.get("userid")
        notification = args.get("notification")
        sessionSta, userID, userName = accountManager.validateSessionID(
            session)
        if sessionSta:
            accountManager.sendNotification(userid, notification)
            return json.dumps({"status": "ok", "message": "ok"})
        else:
            return json.dumps({"status": "error", "message": "Session Invalid!"})
    return json.dumps({"status": "error", "message": "invalid args"})

@app.route('/user/notification/get', methods=['GET'])
def getNotification():
    args = request.args
    if checkArgsExist(args, ["session"]):
        session = args.get("session")
        sessionSta, userID, userName = accountManager.validateSessionID(
            session)
        if sessionSta:
            sta, res = accountManager.getNotification(userID)
            if not sta:
                print("err", userID, " not in ", accountManager.messages)
            return json.dumps({"status": "ok", "message": "ok", "messages": res})
        else:
            return json.dumps({"status": "error", "message": "Session Invalid!"})
    return json.dumps({"status": "error", "message": "invalid args"})

@app.route('/user/register/registerotp', methods=['GET'])
def registerotp():
    args = request.args
    if checkArgsExist(args, ["username", "email", "phone", "password"]):
        if accountManager.checkEmailExist(args.get("email")) or accountManager.checkMobileExist(args.get("phone")):
            return json.dumps({"status": "error", "message": "Account Exist!"})
        status1, otp = accountManager.getOTPEmail(
            args.get("email"), register=True)
        status2, otp = accountManager.getOTPMobile(
            args.get("phone"), register=True)
        if status1 and status2:
            return json.dumps({"status": "ok", "message": "OTP Sent!"})
        else:
            return json.dumps({"status": "error", "message": "Account Exist!"})
    return json.dumps({"status": "error", "message": "invalid args"})


@app.route('/user/register/register', methods=['GET'])
def register():
    args = request.args
    if checkArgsExist(args, ["username", "email", "phone", "password", "otp"]):
        username = args.get("username")
        email = args.get("email")
        phone = args.get("phone")
        password = args.get("password")
        otp = args.get("otp")
        verifyOTPEmail, sta = accountManager.validateOTPEmail(email, otp)
        verifyOTPPhone, sta = accountManager.validateOTPMobile(phone, otp)
        if verifyOTPEmail or verifyOTPPhone:
            result, msg = accountManager.addNewUser(
                username, password, email, phone)
            if result:
                sta, session, userid, msg = accountManager.externalLoginEmail(
                    email)
                return json.dumps({"status": "ok", "message": msg, "session": session})
            else:
                return json.dumps({"status": "error", "message": msg})
        else:
            return json.dumps({"status": "error", "message": "OTP Wrong"})
    return json.dumps({"status": "error", "message": "invalid args"})


@app.route('/user/login/phonewithotp', methods=['GET'])
def phonewithotp():
    args = request.args
    if checkArgsExist(args, ["phone"]):
        status, otp = accountManager.getOTPMobile(args.get("phone"))
        if status:
            return json.dumps({"status": "ok", "message": "OTP Sent!"})
        else:
            return json.dumps({"status": "error", "message": "Account Not Found!"})
    return json.dumps({"status": "error", "message": "invalid args"})


@app.route('/user/login/emailwithotp', methods=['GET'])
def emailwithotp():
    args = request.args
    if checkArgsExist(args, ["email"]):
        status, otp = accountManager.getOTPEmail(args.get("email"))
        if status:
            return json.dumps({"status": "ok", "message": "OTP Sent!"})
        else:
            return json.dumps({"status": "error", "message": "Account Not Found!"})
    return json.dumps({"status": "error", "message": "invalid args"})


@app.route('/user/login/emailvalidateotp', methods=['GET'])
def emailvalidateotp():
    args = request.args
    if checkArgsExist(args, ["email", "otp"]):
        email = args.get("email")
        otp = args.get("otp")
        verifyOTPEmail, sta = accountManager.validateOTPEmail(email, otp)
        if verifyOTPEmail:
            result, sessionIDThis, userID, msg = accountManager.externalLoginEmail(
                email)
            if result:
                return json.dumps({"status": "ok", "message": msg, "session": sessionIDThis})
            else:
                return json.dumps({"status": "error", "message": msg})
        else:
            return json.dumps({"status": "error", "message": "OTP Wrong"})
    return json.dumps({"status": "error", "message": "invalid args"})


@app.route('/user/login/phonevalidateotp', methods=['GET'])
def phonelvalidateotp():
    args = request.args
    if checkArgsExist(args, ["phone", "otp"]):
        mobile = args.get("phone")
        otp = args.get("otp")
        verifyOTPPhone, sta = accountManager.validateOTPMobile(mobile, otp)
        if verifyOTPPhone:
            result, sessionIDThis, userID, msg = accountManager.externalLoginMobile(
                mobile)
            if result:
                return json.dumps({"status": "ok", "message": msg, "session": sessionIDThis})
            else:
                return json.dumps({"status": "error", "message": msg})
        else:
            return json.dumps({"status": "error", "message": "OTP Wrong"})
    return json.dumps({"status": "error", "message": "invalid args"})


@app.route('/user/login/phonewithpassword', methods=['GET'])
def phonewithpassword():
    args = request.args
    if checkArgsExist(args, ["phone", "password"]):
        mobile = args.get("phone")
        password = args.get("password")
        verifyPassword, sessionIDThis, userID, msg = accountManager.validatePhonePassword(
            mobile, password)
        if verifyPassword:
            return json.dumps({"status": "ok", "message": msg, "session": sessionIDThis})
        else:
            return json.dumps({"status": "error", "message": msg})
    return json.dumps({"status": "error", "message": "invalid args"})


@app.route('/user/login/emailwithpassword', methods=['GET'])
def emailwithpassword():
    args = request.args
    if checkArgsExist(args, ["email", "password"]):
        email = args.get("email")
        password = args.get("password")
        verifyPassword, sessionIDThis, userID, msg = accountManager.validateEmailPassword(
            email, password)
        if verifyPassword:
            return json.dumps({"status": "ok", "message": msg, "session": sessionIDThis})
        else:
            return json.dumps({"status": "error", "message": msg})
    return json.dumps({"status": "error", "message": "invalid args"})


@app.route('/user/reset/withemail', methods=['GET'])
def withemail():
    args = request.args
    if checkArgsExist(args, ["email"]):
        status, otp = accountManager.getOTPEmail(args.get("email"))
        if status:
            return json.dumps({"status": "ok", "message": "OTP Sent!"})
        else:
            return json.dumps({"status": "error", "message": "Account Not Exist!"})
    return json.dumps({"status": "error", "message": "invalid args"})


@app.route('/user/reset/withphone', methods=['GET'])
def resetwithphone():
    args = request.args
    if checkArgsExist(args, ["phone"]):
        status, otp = accountManager.getOTPMobile(args.get("phone"))
        if status:
            return json.dumps({"status": "ok", "message": "OTP Sent!"})
        else:
            return json.dumps({"status": "error", "message": "Account Not Exist!"})
    return json.dumps({"status": "error", "message": "invalid args"})


@app.route('/user/reset/emailvalidate', methods=['GET'])
def emailvalidate():
    args = request.args
    if checkArgsExist(args, ["email", "newpassword", "otp"]):
        email = args.get("email")
        newpassword = args.get("newpassword")
        otp = args.get("otp")
        verifyOTPEmail,  sessionID, userID, msg = accountManager.validateEmailOTP(
            email, otp)
        if verifyOTPEmail:
            result, msg = accountManager.updateEmailPassword(
                email, newpassword)
            if result:
                sta, session, userid, msg = accountManager.externalLoginEmail(
                    email)
                return json.dumps({"status": "ok", "message": msg, "session": session})
            else:
                return json.dumps({"status": "error", "message": msg})
        else:
            return json.dumps({"status": "error", "message": "OTP Wrong"})
    return json.dumps({"status": "error", "message": "invalid args"})


@app.route('/user/reset/phonevalidate', methods=['GET'])
def phonevalidate():
    args = request.args
    if checkArgsExist(args, ["phone", "newpassword", "otp"]):
        phone = args.get("phone")
        newpassword = args.get("newpassword")
        otp = args.get("otp")
        verifyOTPEmail,  sessionID, userID, msg = accountManager.validatePhoneOTP(
            phone, otp)
        if verifyOTPEmail:
            result, msg = accountManager.updatePhonePassword(
                phone, newpassword)
            if result:
                sta, session, userid, msg = accountManager.externalLoginMobile(
                    phone)
                return json.dumps({"status": "ok", "message": msg, "session": session})
            else:
                return json.dumps({"status": "error", "message": msg})
        else:
            return json.dumps({"status": "error", "message": "OTP Wrong"})
    return json.dumps({"status": "error", "message": "invalid args"})


@app.route('/party/create', methods=['GET'])
def partycreate():
    args = request.args
    if checkArgsExist(args, ["session", "destination", "destination_coordinate", "arrival_time", "destination_location"]):
        session = args.get("session")
        destination = args.get("destination")
        destination_coordinate = args.get("destination_coordinate")
        arrival_time = args.get("arrival_time")
        destination_location = args.get("destination_location")
        sessionSta, userID, userName = accountManager.validateSessionID(
            session)
        if sessionSta:
            res, partyID, msg = partyManager.createNewParty(
                userID, destination, destination_coordinate, destination_location, arrival_time)
            if res:
                sta, partyID, msg = partyManager.joinParty(partyID, userID)
                if sta:
                    return json.dumps({"status": "ok", "message": msg, "partyid": partyID})
                else:
                    return json.dumps({"status": "error", "message": msg})
            else:
                return json.dumps({"status": "error", "message": msg})
        else:
            return json.dumps({"status": "error", "message": "Session Invalid!"})
    return json.dumps({"status": "error", "message": "invalid args"})


@app.route('/party/join', methods=['GET'])
def partyjoin():
    args = request.args
    if checkArgsExist(args, ["session", "partyid"]):
        session = args.get("session")
        partyid = args.get("partyid")
        sessionSta, userID, userName = accountManager.validateSessionID(
            session)
        if sessionSta:
            res, partyID, msg = partyManager.joinParty(
                partyid, userID)
            if res:
                return json.dumps({"status": "ok", "message": msg, "partyid": partyID})
            else:
                return json.dumps({"status": "error", "message": msg})
        else:
            return json.dumps({"status": "error", "message": "Session Invalid!"})
    return json.dumps({"status": "error", "message": "invalid args"})


@app.route('/party/updateparty', methods=['GET'])
def updateparty():
    args = request.args
    if checkArgsExist(args, ["session", "partyid", "destination", "destination_coordinate", "destination_location", "arrival_time"]):
        session = args.get("session")
        destination = args.get("destination")
        partyid = args.get("partyid")
        destination_coordinate = args.get("destination_coordinate")
        destination_location = args.get("destination_location")
        arrival_time = args.get("arrival_time")
        sessionSta, userID, userName = accountManager.validateSessionID(
            session)
        if sessionSta:
            res, leader, msg = partyManager.checkPartyLeader(partyid)
            if res:
                if str(leader) == str(userID):
                    res, partyID, msg = partyManager.updateParty(
                        partyid, userID, destination, destination_coordinate, destination_location, arrival_time)
                    if res:
                        return json.dumps({"status": "ok", "message": msg, "partyid": partyID})
                    else:
                        return json.dumps({"status": "error", "message": msg})
                else:
                    return json.dumps({"status": "error", "message": "Not Party Leader! Cannot change party info!"})
            else:
                return json.dumps({"status": "error", "message": msg})
        else:
            return json.dumps({"status": "error", "message": "Session Invalid!"})
    return json.dumps({"status": "error", "message": "invalid args"})


@app.route('/party/joinedParties', methods=['GET'])
def getJoinedParties():
    args = request.args
    if checkArgsExist(args, ["session"]):
        session = args.get("session")
        sessionSta, userID, userName = accountManager.validateSessionID(
            session)
        if sessionSta:
            sta, parties = partyManager.checkUserInPartyHistory(
                userID)
            if sta:
                return json.dumps({"status": "ok", "message": "OK!", "parties": parties})
            else:
                return json.dumps({"status": "error", "message": "Error No Parties Joined"})
        else:
            return json.dumps({"status": "error", "message": "Session Invalid!"})
    return json.dumps({"status": "error", "message": "invalid args"})


@app.route('/party/getcoordinate', methods=['GET'])
def partygetcoordinate():
    args = request.args
    if checkArgsExist(args, ["session", "partyid"]):
        session = args.get("session")
        partyid = args.get("partyid")
        sessionSta, userID, userName = accountManager.validateSessionID(
            session)
        if sessionSta:
            sta, userCurrentPartyID, msg = partyManager.checkUserPartyID(
                userID)
            if sta:
                if str(userCurrentPartyID) == str(partyid):
                    sta, partyID, res = partyManager.getPartyStatus(
                        userCurrentPartyID)
                    if sta:
                        return json.dumps({"status": "ok", "message": msg, "partySta": res})
                    else:
                        return json.dumps({"status": "error", "message": "Internal Errror! partygetcoordinate"})
                else:
                    return json.dumps({"status": "error", "message": "User Not In This Party!"})
            else:
                return json.dumps({"status": "error", "message": msg})
        else:
            return json.dumps({"status": "error", "message": "Session Invalid!"})
    return json.dumps({"status": "error", "message": "invalid args"})


@app.route('/party/updatecoordinate', methods=['GET'])
def partyupdatecoordinate():
    args = request.args
    if checkArgsExist(args, ["session", "partyid", "coordinate", "arrival_time"]):
        session = args.get("session")
        partyid = args.get("partyid")
        coordinate = args.get("coordinate")
        arrival_time = args.get("arrival_time")
        sessionSta, userID, userName = accountManager.validateSessionID(
            session)
        if sessionSta:
            sta, userCurrentPartyID, msg = partyManager.checkUserPartyID(
                userID)
            if sta:
                if str(userCurrentPartyID) == str(partyid):
                    sta, partyID, msg = partyManager.updatePartyStatus(
                        userCurrentPartyID, userID, userName, coordinate, arrival_time)
                    if sta:
                        return json.dumps({"status": "ok", "message": msg})
                    else:
                        return json.dumps({"status": "error", "message": msg})
                else:
                    return json.dumps({"status": "error", "message": "User Not In This Party!"})
            else:
                return json.dumps({"status": "error", "message": msg})
        else:
            return json.dumps({"status": "error", "message": "Session Invalid!"})
    return json.dumps({"status": "error", "message": "invalid args"})


@app.route('/party/get', methods=['GET'])
def partyget():
    # PARTY_ID, PARTY_LEADER, DESTINATION, DESTINATION_COORDINATE, PREFERRED_ARRIVAL_TIME, STA
    args = request.args
    if checkArgsExist(args, ["session", "partyid"]):
        session = args.get("session")
        partyid = args.get("partyid")
        sessionSta, userID, userName = accountManager.validateSessionID(
            session)
        if sessionSta:
            sta, userCurrentPartyID, msg = partyManager.checkUserPartyID(
                userID)
            if sta:
                if str(userCurrentPartyID) == str(partyid):
                    sta, partyDetails, res = partyManager.getPartyDetails(
                        userCurrentPartyID)
                    if sta:
                        if len(partyDetails) == 7:
                            # parameters ok
                            PARTY_ID, PARTY_LEADER, DESTINATION, DESTINATION_COORDINATE, DESTINATION_LOCATION, PREFERRED_ARRIVAL_TIME, STA = partyDetails
                            print({"status": "ok", "message": msg, "PARTY_LEADER": PARTY_LEADER, "DESTINATION": DESTINATION, "DESTINATION_LOCATION": DESTINATION_LOCATION, "DESTINATION_COORDINATE":
                                  DESTINATION_COORDINATE, "PREFERRED_ARRIVAL_TIME": PREFERRED_ARRIVAL_TIME.strftime("%Y-%m-%d %H:%M:%S"), "STA": STA})
                            return json.dumps({"status": "ok", "message": msg, "PARTY_LEADER": PARTY_LEADER, "DESTINATION": DESTINATION, "DESTINATION_COORDINATE": DESTINATION_COORDINATE, "DESTINATION_LOCATION": DESTINATION_LOCATION, "PREFERRED_ARRIVAL_TIME": PREFERRED_ARRIVAL_TIME.strftime("%Y-%m-%d %H:%M:%S"), "STA": STA})
                        else:
                            return json.dumps({"status": "error", "message": "Internal Errror! partyget parameters error!"})
                    else:
                        return json.dumps({"status": "error", "message": "Internal Errror! partyget"})
                else:
                    return json.dumps({"status": "error", "message": "User Not In This Party!"})
            else:
                return json.dumps({"status": "error", "message": msg})
        else:
            return json.dumps({"status": "error", "message": "Session Invalid!"})
    return json.dumps({"status": "error", "message": "invalid args"})

@app.route('/party/quit', methods=['GET'])
def partyquit():
    # PARTY_ID, PARTY_LEADER, DESTINATION, DESTINATION_COORDINATE, PREFERRED_ARRIVAL_TIME, STA
    args = request.args
    if checkArgsExist(args, ["session", "partyid"]):
        session = args.get("session")
        partyid = args.get("partyid")
        sessionSta, userID, userName = accountManager.validateSessionID(
            session)
        if sessionSta:
            sta, userCurrentPartyID = partyManager.checkUserInParty(
                userID)
            if sta:
                partyManager.quitPartyCompletely(userID, partyid)
                return json.dumps({"status": "ok", "message": "OK!"})
            else:
                return json.dumps({"status": "error", "message": "User Not In This Party!"})
        else:
            return json.dumps({"status": "error", "message": "Session Invalid!"})
    return json.dumps({"status": "error", "message": "invalid args"})

if __name__ == '__main__':
    app.run(host="0.0.0.0", port=8080)
