import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { createRegistration, getRegistrations, updateRegistration, updateTaskSkills } from "../services";
import Alert from "./Alert";
import { useAuth } from "./AuthProvider";
import FormInput from "./FormInput";
import InfoBox from "./InfoBox";
import Modal from "./Modal";
import RichTextEditor from "./RichTextEditor";
import RichTextViewer from "./RichTextViewer";
import SkillBadge from "./SkillBadge";
import SkillsEditor from "./SkillsEditor";
import CreateBusinessEmail from "./CreateBusinessEmail";

export default function Task({ task, setFetchAmount, businessId, allSkills, isNotAllowedToRegister }) {
    const { authData } = useAuth();
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [error, setError] = useState("");
    const [registrationErrors, setRegistrationErrors] = useState([]);
    const [taskSkillsError, setTaskSkillsError] = useState("");
    const [isRegistrationsModalOpen, setIsRegistrationsModalOpen] = useState(false);
    const [registrations, setRegistrations] = useState([]);
    const [isEditing, setIsEditing] = useState(false);
    const [motivation, setMotivation] = useState("");
    const [canSubmit, setCanSubmit] = useState(true);


    const isOwner = authData.type === "supervisor" && authData.businessId === businessId;

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!canSubmit) {
            return;
        }

        setError("");

        try {
            createRegistration(task.taskId, motivation.trim())
                .then(() => {
                    setIsModalOpen(false);
                    if (setFetchAmount) {
                        setFetchAmount(currentAmount => currentAmount + 1);
                    }
                })
                .catch(error => setError(error.message));
        } catch {
            setError("Er is iets misgegaan bij het versturen van de aanmelding.");
        }
    };

    useEffect(() => {
        if (!isOwner) {
            return;
        }

        let ignore = false;

        getRegistrations(task.taskId)
            .then(data => {
                if (ignore) return;
                setRegistrations(data);
            })
            .catch(error => {
                if (ignore) return;
                setRegistrationErrors((currentErrors) => [...currentErrors, error.message]);
            });

        return () => {
            ignore = true;
        };
    }, [isOwner, task.taskId]);

    const handleRegistrationResponse = (e) => {
        e.preventDefault();
        const formData = new FormData(e.target);
        const userId = parseInt(formData.get("userId"));
        const response = formData.get("response").trim();
        const accepted = e.nativeEvent.submitter.value === "true";

        setRegistrationErrors((currentErrors) => currentErrors.filter((errorObj) => {
            return errorObj.userId !== userId;
        }));

        updateRegistration({
            taskId: task.taskId,
            userId: userId,
            accepted,
            response,
        })
            .then(() => {
                setRegistrations((currentRegistrations) => {
                    return currentRegistrations.filter((registration) => {
                        return registration.student.userId !== userId;
                    });
                });
                setFetchAmount((currentAmount) => currentAmount + 1);
            })
            .catch((error) => {
                setRegistrationErrors((currentErrors) => [...currentErrors, { userId, error: error.message }]);
            });
    }

    const handleSave = async (skills) => {
        const skillIds = skills.map((skill) => skill.skillId);

        setTaskSkillsError("");

        try {
            await updateTaskSkills(task.taskId, skillIds);
            setIsEditing(false)
            setFetchAmount((currentAmount) => currentAmount + 1);
        } catch (error) {
            setTaskSkillsError(error.message);
        }
    };

    return (
        <div id={`task-${task.taskId}`} className="group">
            <div className="target flex flex-col sm:flex-row gap-4 justify-between items-center w-full p-5 bg-white rounded-lg shadow-md border border-gray-300 transition hover:shadow-lg">
                <div className="space-y-3 flex-grow">
                    <h2 className="text-2xl font-bold text-gray-800">
                        {task.title}
                    </h2>
                    <RichTextViewer
                        text={task.description}
                    />
                    <Alert text={taskSkillsError} onClose={() => { setTaskSkillsError("") }} />
                    <SkillsEditor
                        allSkills={allSkills}
                        initialSkills={task.skills}
                        isEditing={isEditing && isOwner}
                        onSave={handleSave}
                        onCancel={() => setIsEditing(false)}
                        setError={setTaskSkillsError}
                        isAllowedToAddSkill={isOwner}
                    >
                        <div className="flex flex-wrap gap-2 items-center">
                            {task.skills.length === 0 && <span>Er zijn geen skills vereist voor deze taak</span>}
                            {task.skills.map((skill) => (
                                <SkillBadge
                                    key={skill.skillId}
                                    skillName={skill.name}
                                    isPending={skill.isPending}
                                />
                            ))}
                            {isOwner && !isEditing && (
                                <button className="btn-secondary py-1 px-3" onClick={() => setIsEditing(true)}>
                                    Aanpassen ✏️
                                </button>
                            )}
                        </div>
                    </SkillsEditor>
                </div>

                <div className="flex flex-col min-w-fit items-end gap-3 mb-auto">
                    <InfoBox>
                        <strong className="text-primary mr-1">
                            {task.totalNeeded - task.totalAccepted}
                        </strong>
                        van de {task.totalNeeded} plekken beschikbaar
                    </InfoBox>
                    <InfoBox>
                        <strong className="text-primary mr-1">
                            {task.totalRegistered}
                        </strong>
                        openstaande aanmeldingen
                    </InfoBox>
                    {authData.type === "student" && (
                        <button className={`btn-primary w-full ${isNotAllowedToRegister ? "cursor-not-allowed opacity-50" : ""}`} disabled={isNotAllowedToRegister} onClick={() => setIsModalOpen(true)}>{isNotAllowedToRegister ? "Aanmelding ontvangen" : "Aanmelden"}</button>
                    )}
                    {isOwner && (<>
                        <button className="btn-primary w-full" onClick={() => setIsRegistrationsModalOpen(true)}>Bekijk aanmeldingen</button>
                        <CreateBusinessEmail taskId={task.taskId} />
                    </>
                    )}
                </div>
            </div>
            {!isNotAllowedToRegister && (
                <Modal
                    modalHeader={`Aanmelden voor ${task.title}`}
                    isModalOpen={isModalOpen}
                    setIsModalOpen={setIsModalOpen}
                >
                    <form className="p-4" onSubmit={handleSubmit}>
                        <div className="flex flex-col gap-4">
                            <Alert text={error} />
                            <RichTextEditor
                                label="Motivatiebrief"
                                onSave={setMotivation}
                                setCanSubmit={setCanSubmit}
                            />
                            <div className="col-span-2">
                            </div>
                            <div className="flex gap-4 flex-wrap">
                                <button type="button" className="flex-1 btn-secondary" onClick={() => setIsModalOpen(false)}>Annuleren</button>
                                <button type="submit" className="flex-1 btn-primary">Verzenden</button>
                            </div>
                        </div>
                    </form>
                </Modal>
            )}

            {isOwner && (
                <Modal maxWidth={"max-w-2xl"} modalHeader={`Aanmeldingen voor "${task.title}"`} isModalOpen={isRegistrationsModalOpen} setIsModalOpen={setIsRegistrationsModalOpen}>
                    {registrations.length === 0 && (
                        <p>Er zijn nog geen aanmeldingen voor deze taak</p>
                    )}
                    <div className="flex flex-col gap-4">
                        {registrations.map((registration) => (
                            <InfoBox key={registration.student.userId} className="flex flex-col gap-2 px-4 py-4">
                                <div>
                                    <Link to={`/profile/${registration.student.userId}`} className="inline-flex gap-2 text-lg font-bold underline text-black hover:text-primary" target="_blank" rel="noopener noreferrer">
                                        <svg className="w-4" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512" fill="currentColor"><path d="M224 256A128 128 0 1 0 224 0a128 128 0 1 0 0 256zm-45.7 48C79.8 304 0 383.8 0 482.3C0 498.7 13.3 512 29.7 512l388.6 0c16.4 0 29.7-13.3 29.7-29.7C448 383.8 368.2 304 269.7 304l-91.4 0z" /></svg>
                                        {registration.student.username}
                                    </Link>
                                    <RichTextViewer text={registration.reason} />
                                </div>
                                <div className="flex flex-wrap items-center gap-2 my-3">
                                    <span>Skills:</span>
                                    {registration.student.skills.map((skill) => (
                                        <SkillBadge
                                            key={skill.skill.skillId}
                                            skillName={skill.skill.name}
                                            isPending={skill.skill.isPending}
                                        />
                                    ))}
                                </div>
                                <Alert text={registrationErrors.find((errorObj) => errorObj.userId === registration.student.userId)?.error} />
                                <form onSubmit={handleRegistrationResponse}>
                                    <FormInput label={`Reden (optioneel)`} max={400} min={0} type="textarea" name={`response`} rows={1} />
                                    <input type="hidden" name="userId" value={registration.student.userId} />
                                    <div className="flex gap-2 flex-wrap mt-3">
                                        <button type="submit" value={true} className="btn px-3 bg-green-700 hover:bg-green-800 focus:ring-green-500 text-white">Accepteren</button>
                                        <button type="submit" value={false} className="btn px-3 bg-red-600 hover:bg-red-700 focus:ring-red-200 text-white">Weigeren</button>
                                    </div>
                                </form>
                            </InfoBox>
                        ))}
                    </div>
                    <div className="flex justify-center mt-4">
                        <button className="btn-primary" onClick={() => setIsRegistrationsModalOpen(false)}>Sluiten</button>
                    </div>
                </Modal>
            )}
        </div>
    );
}

