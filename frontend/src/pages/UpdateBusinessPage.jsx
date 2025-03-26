import { useEffect, useState } from 'react';
import { useNavigate } from "react-router-dom";
import Alert from '../components/Alert';
import { useAuth } from '../components/AuthProvider';
import Card from '../components/Card';
import DragDrop from '../components/DragDrop';
import FormInput from '../components/FormInput';
import RichTextEditor from '../components/RichTextEditor';
import { createErrorMessage, getBusiness, updateBusiness } from '../services';
import useFetch from '../useFetch';

/**
 * Creates a UpdateBusinessPage component
 */
export default function UpdateBusinessPage() {
    const { authData } = useAuth();
    const [error, setError] = useState();
    const [nameError, setNameError] = useState();
    const [description, setDescription] = useState();
    const [descriptionError, setDescriptionError] = useState();
    const [locationError, setLocationError] = useState();
    const navigation = useNavigate();

    useEffect(() => {
        if (!authData.isLoading && authData.type !== 'supervisor') {
            navigation("/not-found");
        }
    }, [authData])

    const { data: business } = useFetch(async () => !authData.isLoading && await getBusiness(authData.businessId), [authData.businessId]);
    if (business?.description !== undefined && description === undefined) {
        setDescription(business?.description);
    }

    function onSubmit(event) {
        event.preventDefault();
        if (nameError != undefined || descriptionError != undefined || locationError != undefined) {
            return;
        }

        const formData = new FormData(event.target);
        const photo = formData.get("photos");
        if (photo instanceof File && photo.size === 0 && photo.name.length === 0) {
            formData.delete("photos");
        }

        formData.append("description", description);

        updateBusiness(formData)
            .then(() => {
                navigation(`/business/${authData.businessId}`);
            }).catch(error =>
                setError(createErrorMessage(
                    error,
                    {
                        401: "Je bent niet ingelogd om de bedrijfspagina aan te passen",
                        403: "Je bent niet geauthoriseerd om de bedrijfspagina aan te passen",
                        404: "De bedrijfspagina kan niet gevonden worden",
                    }
                ))
            );
    }

    return (
        <form onSubmit={onSubmit} className="max-w-2xl mx-auto">
            <Card header="Bedrijf aanpassen" className="flex flex-col gap-3 px-6 py-12 sm:rounded-lg sm:px-12 shadow-xl border border-gray-300">
                <Alert text={error} />
                <FormInput
                    label="Bedrijfsnaam"
                    type="text"
                    name="name"
                    initialValue={business?.name}
                    error={nameError}
                    setError={setNameError}
                    max={50}
                    required={true}
                />
                {description !== undefined
                    ? <RichTextEditor
                        label="Beschrijving"
                        max={4000}
                        required
                        defaultText={description}
                        onSave={setDescription}
                        error={descriptionError}
                        setError={setDescriptionError}
                    />
                    : <div className="flex justify-center">
                        <img src="/loading.gif" alt="loading" />
                    </div>
                }
                <FormInput
                    label="Locatie"
                    type="text"
                    name="location"
                    initialValue={business?.location}
                    error={locationError}
                    setError={setLocationError}
                    max={255}
                    required={true}
                />
                <DragDrop multiple={false} name="photos" initialFilePath={business?.photo?.path} />
                <div className='grid grid-cols-2 gap-2'>
                    <button className="btn-secondary flex-grow" type="button" onClick={() => navigation(-1)}>Annuleren</button>
                    <button className="btn-primary flex-grow" type="submit">Opslaan</button>
                </div>
            </Card>
        </form>
    );
}
