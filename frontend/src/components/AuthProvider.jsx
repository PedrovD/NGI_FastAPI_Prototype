import { createContext, useContext, useState, useEffect } from 'react';
import { getAuthorization } from '../services';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [authData, setAuthData] = useState({ type: "none", userId: null, businessId: null, isLoading: true });

    useEffect(() => {
        // Check if user is authenticated on component mount
        const checkAuth = async () => {
            try {
                const auth = await getAuthorization();
                setAuthData({
                    ...auth,
                    isLoading: false
                });
            } catch (error) {
                console.error("Authentication check failed:", error);
                setAuthData({
                    type: "none",
                    userId: null,
                    businessId: null,
                    isLoading: false
                });
            }
        };

        checkAuth();
    }, []);

    return (
        <AuthContext.Provider value={{ authData, setAuthData }}>
            {children}
        </AuthContext.Provider>
    );
};

/**
 * @returns { { authData: Awaited<ReturnType<typeof getAuthorization>> & { isLoading: boolean } }}
 */
// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = () => useContext(AuthContext);
