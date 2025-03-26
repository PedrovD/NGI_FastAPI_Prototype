import BusinessProjectDashboard from './BusinessProjectDashboard';

export default function DashboardsOverview({ businesses }) {
    return (
        <div className="flex flex-col gap-16">
            {businesses.map((businessInformation) => (
                <BusinessProjectDashboard
                    key={businessInformation.business.businessId}
                    business={businessInformation.business}
                    topSkills={businessInformation.topSkills}
                    projects={businessInformation.projects}
                />
            ))}
        </div>
    );
}

