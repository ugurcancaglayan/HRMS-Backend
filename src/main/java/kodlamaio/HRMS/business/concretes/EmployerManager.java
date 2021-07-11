package kodlamaio.HRMS.business.concretes;

import kodlamaio.HRMS.business.abstracts.EmployerService;
import kodlamaio.HRMS.business.abstracts.VerificationCodeService;
import kodlamaio.HRMS.core.utilities.results.*;
import kodlamaio.HRMS.dataAccess.abstracts.EmployerDao;
import kodlamaio.HRMS.dataAccess.abstracts.VerificationCodeDao;
import kodlamaio.HRMS.entities.concretes.Employer;
import kodlamaio.HRMS.entities.concretes.JobAdvertisement;
import kodlamaio.HRMS.entities.concretes.JobSeeker;
import kodlamaio.HRMS.entities.concretes.VerificationCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmployerManager implements EmployerService {

    private EmployerDao employerDao;
    private VerificationCodeService verificationCodeService;
    private VerificationCodeDao verificationCodeDao;

    @Autowired
    public EmployerManager(EmployerDao employerDao, VerificationCodeService verificationCodeService, VerificationCodeDao verificationCodeDao) {
        this.employerDao = employerDao;
        this.verificationCodeService = verificationCodeService;
        this.verificationCodeDao = verificationCodeDao;
    }

    public boolean checkEmail(Employer employer) {
        String regex = "^(.+)@(.+)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(employer.getEmail());
        if (!matcher.matches()) {
            new ErrorResult("Geçersiz Email Adresi");
            return false;
        } else if (!employer.getEmail().contains(employer.getWebSite())) {
            new ErrorResult("Domain adresi girmek zorundasınız");
            return false;
        }
        return true;
    }

    private boolean checkPhoneNumber(Employer employer) {
        String patterns
                = "^(\\+\\d{1,3}( )?)?((\\(\\d{3}\\))|\\d{3})[- .]?\\d{3}[- .]?\\d{4}$"
                + "|^(\\+\\d{1,3}( )?)?(\\d{3}[ ]?){2}\\d{3}$"
                + "|^(\\+\\d{1,3}( )?)?(\\d{3}[ ]?)(\\d{2}[ ]?){2}\\d{2}$";

        Pattern pattern = Pattern.compile(patterns);
        Matcher matcher = pattern.matcher(employer.getPhoneNumber());
        if (!matcher.matches()) {
            return false;
        }
        return true;
    }

    public boolean checkNullInfoForEmployer(Employer employer, String confirmPassword) {
        if (employer.getEmail() != null &&
            employer.getCompanyName() != null &&
            employer.getPassword() != null &&
            employer.getPhoneNumber() != null &&
            confirmPassword != null) {
            return true;
        }
        return false;
    }

    @Override
    public Result add(Employer employer, String confirmPassword) {

        if (!checkNullInfoForEmployer(employer, confirmPassword)) {
            return new ErrorResult("Eksik bilgiler mevcut. " +
                    "Tüm boşlukları doldurunuz!");
        }
        if (!checkEmail(employer)) {
            return new ErrorResult("Email hatası!");
        }
        if (!checkPhoneNumber(employer)) {
            return new ErrorResult("Geçerli bir telefon numarası giriniz!");
        }
        if (employerDao.findByEmail(employer.getEmail()) != null) {
            return new ErrorResult("Bu e-posta adresi zaten kayıtlı!");
        }
        if (!employer.getPassword().equals(confirmPassword)) {
            return new ErrorResult("Şifreler uyuşmuyor!");
        }

        this.employerDao.save(employer);
        this.verificationCodeService.generateCode(new VerificationCode(), employer.getId());
        return new SuccessResult("İş veren eklendi!");
    }

    @Override
    public DataResult<List<Employer>> getAll() {
        return new SuccessDataResult<List<Employer>>(
                this.employerDao.findAll(),
                "İş veren kullanıcılar listelendi!");
    }
}
