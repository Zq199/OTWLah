from email.message import EmailMessage
from email.mime.text import MIMEText
import threading
import smtplib


class EmailService():
    def __init__(self):
        self.otpTemplate = '''<div style="font-family: Helvetica,Arial,sans-serif;min-width:1000px;overflow:auto;line-height:2">
  <div style="margin:50px auto;width:70%;padding:20px 0">
    <div style="border-bottom:1px solid #eee">
      <a href="" style="font-size:1.4em;color: #00466a;text-decoration:none;font-weight:600">OTW Lah</a>
    </div>
    <p style="font-size:1.1em">Hi,</p>
    <p>Thank you for using OTW Lah. Use the following OTP to complete your Sign Up or Login. <br>OTP is valid for 5 minutes</p>
    <h2 style="background: #00466a;margin: 0 auto;width: max-content;padding: 0 10px;color: #fff;border-radius: 4px;">OTPCODEHERE</h2>
    <p style="font-size:0.9em;">Regards,<br />OTW Lah</p>
    <hr style="border:none;border-top:1px solid #eee" />
    <div style="float:right;padding:8px 0;color:#aaa;font-size:0.8em;line-height:1;font-weight:300">
      <p>OTW Lah</p>
      <p>Nanyang Technological University</p>
      <p>SC2006 Team</p>
    </div>
  </div>
</div>'''

    def sendOTPThread(self, reciever, otpcode):
        self.sender = "ntu-sc2006@outlook.com"
        self.recipient = reciever
        self.content = self.otpTemplate.replace("OTPCODEHERE", str(otpcode))
        self.email = MIMEText(self.content, 'html')
        self.email["From"] = self.sender
        self.email["To"] = self.recipient
        self.email["Subject"] = "[OTW Lah] Your OTP"

        self.smtp = smtplib.SMTP("smtp.office365.com", port=587)
        self.smtp.starttls()
        self.smtp.login(self.sender, "Sc2006NTU_EDU")
        self.smtp.sendmail(self.sender, self.recipient, self.email.as_string())
        self.smtp.quit()
        print("OTP Sent to", reciever)

    def sendOTP(self, reciever, otpcode):
        self.sendThread = threading.Thread(
            target=self.sendOTPThread, args=[reciever, otpcode])
        self.sendThread.start()


if __name__ == "__main__":
    emailService = EmailService()
    emailService.sendOTP("fant0004@e.ntu.edu.sg", 654321)
